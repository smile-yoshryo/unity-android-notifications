#import "LocalNotifications.h"
#import <UIKit/UIKit.h>

NSMutableDictionary *registeredThisLaunch = nil;

void registerCategory(struct NotificationStruct *notifStruct) {
    // 同一カテゴリの重複登録防止
    NSString *categoryIdentifier = [NSString stringWithUTF8String:notifStruct->category];
    if (registeredThisLaunch == nil)
        registeredThisLaunch = [NSMutableDictionary dictionary];
    else if ([registeredThisLaunch objectForKey:categoryIdentifier] != nil)
        return;
    [registeredThisLaunch setValue:@YES forKey:categoryIdentifier];

    float version = [[[UIDevice currentDevice] systemVersion] floatValue];
    if (version >= 10.0){
        UNUserNotificationCenter *center = [UNUserNotificationCenter currentNotificationCenter];
        [center requestAuthorizationWithOptions:(UNAuthorizationOptionAlert |
                                                 UNAuthorizationOptionBadge |
                                                 (notifStruct->sound ? UNAuthorizationOptionSound : 0))
                              completionHandler:^(BOOL granted, NSError * _Nullable error) {
            if(error){
                NSLog( @"Push registration FAILED" );
                NSLog( @"ERROR: %@ - %@", error.localizedFailureReason, error.localizedDescription );
                NSLog( @"SUGGESTIONS: %@ - %@", error.localizedRecoveryOptions, error.localizedRecoverySuggestion );
                return;
            }
            NSLog( @"Push registration SUCESS!!" );
            
            // actions
            struct NotificationActionStruct *actionStructs = &notifStruct->action1;
            if (notifStruct->actionCount > 0 && actionStructs != nil) {
                NSMutableArray<UNNotificationAction *> *actions = [NSMutableArray array];
                for (int i = 0; i < MIN(4, notifStruct->actionCount); i++) {
                    struct NotificationActionStruct actionStruct = actionStructs[i];
                    NSString *actTitle = [NSString stringWithUTF8String:actionStruct.title];
                    NSString *gameObject = [NSString stringWithUTF8String:actionStruct.gameObject];
                    if (gameObject == nil) {
                        gameObject = @"";
                    }
                    NSString *handlerMethod = [NSString stringWithUTF8String:actionStruct.handlerMethod];
                    if (handlerMethod == nil){
                        handlerMethod = @"";
                    }
                    NSString *identifier = [NSString stringWithUTF8String:actionStruct.identifier];
                    if (identifier == nil) {
                        identifier = actTitle;
                    }
                    NSString *actId = [NSString stringWithFormat:@"%@:%@:%@",gameObject,handlerMethod,identifier];
                    UNNotificationAction *action = [UNNotificationAction actionWithIdentifier:actId
                                                                                        title:actTitle
                                                                                        options:UNNotificationActionOptionForeground];
                    [actions addObject:action];
                }
                UNNotificationCategory *category = [UNNotificationCategory categoryWithIdentifier:categoryIdentifier
                                                                                          actions:actions
                                                                                intentIdentifiers:@[]
                                                                                          options:UNNotificationCategoryOptionCustomDismissAction];
                NSSet *categories = [NSSet setWithObject:category];
                [center setNotificationCategories:categories];
                
                NSLog( @"Push registration category SUCESS!!" );
            }
        }];
    }else{
        // permissions
        UIUserNotificationType types = UIUserNotificationTypeBadge | UIUserNotificationTypeAlert;
        if (notifStruct->sound) {
            types |= UIUserNotificationTypeSound;
        }

        // actions
        UIMutableUserNotificationCategory *category = [[UIMutableUserNotificationCategory alloc] init];
        category.identifier = categoryIdentifier;
        
        if (notifStruct->actionCount > 0) {
            NSMutableArray<UIUserNotificationAction *> *actions = [NSMutableArray array];
            struct NotificationActionStruct *actionStructs = &notifStruct->action1;
            for (int i = 0; i < MIN(4, notifStruct->actionCount); i++) {
                struct NotificationActionStruct actionStruct = actionStructs[i];
                UIMutableUserNotificationAction *action = [[UIMutableUserNotificationAction alloc] init];
                action.title = [NSString stringWithUTF8String:actionStruct.title];
                NSString *gameObject = [NSString stringWithUTF8String:actionStruct.gameObject];
                if (gameObject == nil)
                    gameObject = @"";
                NSString *handlerMethod = [NSString stringWithUTF8String:actionStruct.handlerMethod];
                if (handlerMethod == nil)
                    handlerMethod = @"";
                NSString *identifier = [NSString stringWithUTF8String:actionStruct.identifier];
                if (identifier == nil)
                    identifier = action.title;
                action.identifier = [NSString stringWithFormat:@"%@:%@:%@", gameObject, handlerMethod, identifier];
                action.activationMode = actionStruct.foreground ? UIUserNotificationActivationModeForeground : UIUserNotificationActivationModeBackground;
                [actions addObject:action];
            }

            [category setActions:actions forContext:UIUserNotificationActionContextDefault];

            NSArray<UIUserNotificationAction *> *minimalActions = [actions subarrayWithRange:NSMakeRange(0, MIN(2, actions.count))];
            [category setActions:minimalActions forContext:UIUserNotificationActionContextMinimal];
        }

        NSMutableSet<UIUserNotificationCategory *> *categories = [NSMutableSet set];
        [categories addObject:category];

        UIUserNotificationSettings *settings = [UIUserNotificationSettings settingsForTypes:types categories: categories];
        [[UIApplication sharedApplication] registerUserNotificationSettings:settings];
    }
}

NSString* findSoundResourceForName(NSString *soundName) {
    soundName = [@"Data/Raw/" stringByAppendingPathComponent:soundName];
    for (NSString *extension in @[@"wav", @"caf", @"aif", @"aiff"]) {
        NSString *soundPath = [[NSBundle mainBundle] pathForResource:soundName ofType:extension];
        if (soundPath != nil) {
            return [soundName stringByAppendingPathExtension:extension];
        }
    }
    return nil;
}

void scheduleNotification(struct NotificationStruct *notifStruct) {
    registerCategory(notifStruct);
    cancelNotification(notifStruct->identifier);

    // UILocalNotificationはiOS10以降では非推奨、動かない
    float version = [[[UIDevice currentDevice] systemVersion] floatValue];
    if (version >= 10.0){
        UNMutableNotificationContent* content = [[UNMutableNotificationContent alloc] init];
        content.body = [NSString stringWithUTF8String:notifStruct->message];
        if (notifStruct->soundName) {
            NSString *soundName = findSoundResourceForName([NSString stringWithUTF8String:notifStruct->soundName]);
            content.sound = [UNNotificationSound soundNamed:soundName];
        }
        content.userInfo = @{@"identifier": [NSNumber numberWithInteger:notifStruct->identifier]};
        
        UNTimeIntervalNotificationTrigger *trigger = [UNTimeIntervalNotificationTrigger triggerWithTimeInterval:((NSTimeInterval)notifStruct->delay) repeats:NO];
        UNNotificationRequest* request = [UNNotificationRequest requestWithIdentifier:[NSString stringWithFormat:@"%d", notifStruct->identifier] content:content trigger:trigger];
        [UNUserNotificationCenter.currentNotificationCenter addNotificationRequest:request withCompletionHandler:^(NSError * _Nullable error) {}];
    }else{
        UILocalNotification *notification = [[UILocalNotification alloc] init];
        notification.alertBody = [NSString stringWithUTF8String:notifStruct->message];
    //    notification.alertAction = "open" // text that is displayed after "slide to..." on the lock screen - defaults to "slide to view"
        notification.fireDate = [NSDate dateWithTimeIntervalSinceNow:((NSTimeInterval)notifStruct->delay)];
        notification.repeatInterval = notifStruct->repeat;
        if (notifStruct->soundName) {
            notification.soundName = findSoundResourceForName([NSString stringWithUTF8String:notifStruct->soundName]);
        }
        notification.userInfo = @{@"identifier": [NSNumber numberWithInteger:notifStruct->identifier]};
        notification.category = [NSString stringWithUTF8String:notifStruct->category];

        [UIApplication.sharedApplication scheduleLocalNotification:notification];
    }
}

void cancelNotification(int identifier) {
    float version = [[[UIDevice currentDevice] systemVersion] floatValue];
    if (version >= 10.0){
        [UNUserNotificationCenter.currentNotificationCenter removeAllPendingNotificationRequests];
        [UNUserNotificationCenter.currentNotificationCenter removeAllDeliveredNotifications];
    }else{
        for (UILocalNotification *notification in UIApplication.sharedApplication.scheduledLocalNotifications) {
            NSNumber *notificationIdentifier = [notification.userInfo objectForKey:@"identifier"];
            if (notificationIdentifier.intValue == identifier) {
                [UIApplication.sharedApplication cancelLocalNotification:notification];
            }
        }
    }
}

void cancelAllNotifications() {
    [UIApplication.sharedApplication cancelAllLocalNotifications];
}
