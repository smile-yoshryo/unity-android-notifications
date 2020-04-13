#import <UIKit/UIKit.h>
#import <Foundation/NSURLConnection.h>
#import <Foundation/NSURL.h>
#import <UserNotifications/UserNotifications.h>

#import "UnityAppController.h"

extern bool _unityAppReady;

@interface LocalNotificationAppController : UnityAppController<UNUserNotificationCenterDelegate>
{}
- (void)application:(UIApplication *)application handleActionWithIdentifier:(NSString *)identifier forLocalNotification:(UILocalNotification *)notification completionHandler:(void (^)())completionHandler;
@end

@implementation LocalNotificationAppController
- (void)application:(UIApplication *)application handleActionWithIdentifier:(NSString *)identifier forLocalNotification:(UILocalNotification *)notification completionHandler:(void (^)())completionHandler
{
    [self notifyUnityOfAction:identifier inNotification:notification completionHandler:completionHandler];
}

- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary*)launchOptions
{
    [super application:application didFinishLaunchingWithOptions:launchOptions];
    
    // iOS 10でレシーブを受け取るための対応
    float version = [[[UIDevice currentDevice] systemVersion] floatValue];
    if (version >= 10.0) {
        [UNUserNotificationCenter currentNotificationCenter].delegate = self;
    }
    return YES;
}

- (void)notifyUnityOfAction:(NSString*)identifier inNotification:(UILocalNotification*)notification completionHandler:(void (^)())completionHandler
{
    if (_unityAppReady)
    {
        NSArray *parts = [identifier componentsSeparatedByString:@":"];
        if (parts.count == 3) {
            NSString *gameObject = parts[0];
            NSString *handlerMethod = parts[1];
            NSString *action = parts[2];
            UnitySendMessage(strdup([gameObject UTF8String]), strdup([handlerMethod UTF8String]), strdup([action UTF8String]));
            UnityBatchPlayerLoop();
        }

        if (completionHandler != nil)
            completionHandler();
    }
    else
    {
        NSNotificationCenter * __weak center = [NSNotificationCenter defaultCenter];
        id __block token = [center addObserverForName:@"UnityReady" object:nil queue:[NSOperationQueue mainQueue] usingBlock:^(NSNotification *note) {
            _unityAppReady = true;
            [center removeObserver:token];
            [self notifyUnityOfAction:identifier inNotification:notification completionHandler:completionHandler];
        }];
    }
}

// ローカルプッシュ受け取り
-(void)application:(UIApplication *)application didReceiveLocalNotification:(UILocalNotification *)notification {
    float version = [[[UIDevice currentDevice] systemVersion] floatValue];
    if (version < 10.0) {
        NSLog(@"通知受信");
    }
}

// プッシュ通知タップ時(ios10)
- (void)userNotificationCenter:(UNUserNotificationCenter *)center didReceiveNotificationResponse:(UNNotificationResponse *)response withCompletionHandler:(void (^)())completionHandler {
    float version = [[[UIDevice currentDevice] systemVersion] floatValue];
    if (version >= 10.0) {
        NSLog(@"recived");
    }
    completionHandler();
}

// プッシュ受け取りフォワグラウンド(ios10)
- (void)userNotificationCenter:(UNUserNotificationCenter *)center willPresentNotification:(UNNotification *)notification withCompletionHandler:(void (^)(UNNotificationPresentationOptions options)) completionHandler {
    
    if([notification.request.trigger isKindOfClass:[UNPushNotificationTrigger class]]) {
        
    }
    else {
        
    }
    completionHandler(UNNotificationPresentationOptionNone);
    
    NSLog(@"受信？");
}
@end

IMPL_APP_CONTROLLER_SUBCLASS(LocalNotificationAppController)
