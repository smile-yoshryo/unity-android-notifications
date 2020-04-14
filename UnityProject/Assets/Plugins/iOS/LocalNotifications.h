#import <Foundation/Foundation.h>

struct NotificationActionStruct {
    char* identifier;
    char* title;
    bool foreground;
    char* gameObject;
    char* handlerMethod;
};

typedef struct {
    char* category;
    bool sound;    
    int actionCount;
    struct NotificationActionStruct action1;
    struct NotificationActionStruct action2;
    struct NotificationActionStruct action3;
    struct NotificationActionStruct action4;
} NotificationStructBase;

struct NotificationStruct {
    int identifier;
    char* message;
    NSTimeInterval delay;
    NSCalendarUnit repeat;
    char* category;
    bool sound;
    char* soundName;
    int actionCount;
    struct NotificationActionStruct action1;
    struct NotificationActionStruct action2;
    struct NotificationActionStruct action3;
    struct NotificationActionStruct action4;
};

struct CalendarNotificationStruct {
    int identifier;
    char* message;
    int year;
    int month;
    int day;
    int hour;
    int minute;
    bool isRepeat;
    char* category;
    bool sound;
    char* soundName;
    int actionCount;
    struct NotificationActionStruct action1;
    struct NotificationActionStruct action2;
    struct NotificationActionStruct action3;
    struct NotificationActionStruct action4;
};

#ifdef __cplusplus
extern "C" {
#endif

    void scheduleNotification(struct NotificationStruct *notifStruct);
    void scheduleCalendarNotification(struct CalendarNotificationStruct *notifStruct);
    void cancelNotification(int identifier);
    void cancelAllNotifications();

#ifdef __cplusplus
}
#endif
