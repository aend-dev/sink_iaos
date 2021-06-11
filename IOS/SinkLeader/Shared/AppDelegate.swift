//
//  AppDelegate.swift
//  AllBaBo
//
//  Created by Aend on 2020/10/15.
//  Copyright © 2020 AllBaBo. All rights reserved.
//

import UIKit
import Firebase
import UserNotifications
import CoreLocation

import WebKit

@UIApplicationMain
class AppDelegate: UIResponder, UIApplicationDelegate, UNUserNotificationCenterDelegate, MessagingDelegate, CLLocationManagerDelegate{
    var token = CommonUtil.string("token") ?? ""
    {
        didSet {
            if isAutoLogin {
                CommonUtil.setString(token, forKey: "token")
            }
        }
    }
    
    var RETOKEN = ""
    
    var USER_NAME = ""
    var USER_SEQ = ""
    var USER_LASTDATE = ""
    var USER_IMG = ""
    var CENTER_SEQ = ""
    var USER_TYPE = ""
    var USER_GRADE = ""

    var fcm_token = CommonUtil.string("fcm_token") ?? "" {
        didSet {
            CommonUtil.setString(fcm_token, forKey: "fcm_token")
        }
    }
    
    var userID = CommonUtil.string("userID") ?? "" {
        didSet {
            CommonUtil.setString(userID, forKey: "userID")
        }
    }
    
    var isAutoLogin = false
    
    var mLocMng: CLLocationManager!
    var mLastLoc: CLLocation!
    var myGps = "0,0"

    var customVC: BaseVC?
    var window: UIWindow?
    
    var commonProcessPool : WKProcessPool!
    
    var isAllOrientation = false
    
    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) -> Bool {
        FirebaseApp.configure()
        
        FCMController.instance.setdelegate(self)
        FCMController.instance.setServerURL("")
        FCMController.instance.registerNoti(application)
        
        startLocation()
        
        commonProcessPool = WKProcessPool.init()

        return true
    }
    func applicationDidEnterBackground(_ application: UIApplication) {
        disconnectFcm()
        print("Webview","applicationDidEnterBackground")
    }

    func applicationWillEnterForeground(_ application: UIApplication) {
        connectFcm()
        print("Webview","applicationWillEnterForeground")
    }
    
    func applicationDidBecomeActive(_ application: UIApplication) {
        if customVC != nil {
            DispatchQueue.main.async {
                //code
                NotificationCenter.default.post(name: UIApplication.didBecomeActiveNotification, object: nil)
            }

        }
    }
    
    func application(_ application: UIApplication, supportedInterfaceOrientationsFor window: UIWindow?) -> UIInterfaceOrientationMask {
        if isAllOrientation {
            return UIInterfaceOrientationMask.all
        }
        
        return UIInterfaceOrientationMask.portrait
    }
    
    func application(_ application: UIApplication, didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data) {
        Messaging.messaging().apnsToken = deviceToken
    }
    
    func application(_ application: UIApplication, didFailToRegisterForRemoteNotificationsWithError error: Error) {
        print("didFailToRegisterForRemoteNotificationsWithError" )
    }
    
    func applicationWillTerminate(_ application: UIApplication) {
        // Called when the application is about to terminate. Save data if appropriate. See also applicationDidEnterBackground:.
        stopLocation()
    }
    
    
    //////////////////////////////////////////////////////////////////////
    // MARK: - MessagingDelegate
    //////////////////////////////////////////////////////////////////////
    
    func messaging(_ messaging: Messaging, didReceiveRegistrationToken fcmToken: String?) {
        print("Firebase registration token: \(fcmToken)")
    }
    
    /////////////////////////////////////////////////////////////////////
    // MARK: - UNUserNotificationCenterDelegate
    //////////////////////////////////////////////////////////////////////
    @available(iOS 10.0, *)
    func userNotificationCenter(_ center: UNUserNotificationCenter, willPresent notification: UNNotification, withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void) {
        let userInfo = notification.request.content.userInfo
//        phasePush(userInfo)
        completionHandler([.alert, .badge, .sound])

//        UNUserNotificationCenter.current().removeNotifications(whereKey: "", hasValue: 2)
    }

    @available(iOS 10.0, *)
    func userNotificationCenter(_ center: UNUserNotificationCenter, didReceive response: UNNotificationResponse, withCompletionHandler completionHandler: @escaping () -> Void) {
//        let center = UNUserNotificationCenter.current()
//        center.removeAllPendingNotificationRequests() // To remove all pending notifications which are not delivered yet but scheduled.
//        center.removeDeliveredNotifications(withIdentifiers: [String]) // To remove all delivered notifications
        let userInfo = response.notification.request.content.userInfo
//        let identifier = response.notification.request.identifier

        phasePush(userInfo)
        completionHandler()
    }

    func disconnectFcm() {
        print("Disconnected from FCM.")
    }

    @objc func refreshToken(_ notification: NSNotification) {
        if let token = Messaging.messaging().fcmToken {
            print("FCM token: \(token)")
            fcm_token = token
            CommonUtil.setString(token, forKey: "fcm_token")
        }

        connectFcm()
    }

    func connectFcm() {
        print("Connected to FCM.")
    }


    func phasePush(_ info: [AnyHashable : Any]) {
        print("phasePush : " ,info)

        if let url_str = (info["gcm.notification.url"] as? String){
            print(url_str)
            if url_str.isValidURL(){
                let urls = URL(string: url_str)!
                UIApplication.shared.open(urls, options: [:], completionHandler: nil)
            }else{
                let url = FrontList[Net.FrontIndex] + "/home?" + url_str
                customVC?.gotoIntro(url: url)
            }
        }
    }
    
    //////////////////////////////////////////////////////////////////////
    // MARK: - CLLocationManagerDelegate
    //////////////////////////////////////////////////////////////////////
    func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
        if mLastLoc == nil || mLastLoc.distance(from: locations.last!) > 500 {
            mLastLoc = locations.last
            myGps = String(format: "%f,%f", locations.last!.coordinate.longitude as Double, locations.last!.coordinate.latitude as Double)
        }
    }

    func locationManager(_ manager: CLLocationManager, didChangeAuthorization status: CLAuthorizationStatus) {
        if status == .denied {
            myGps = "0,0"
        }
    }
    
    func startLocation() {
        mLocMng = CLLocationManager()
        if CLLocationManager.authorizationStatus() == .notDetermined {
            mLocMng.requestWhenInUseAuthorization()
        }
                                                                  
        mLocMng.delegate = self
        mLocMng.startUpdatingLocation()
    }
    
    func stopLocation() {
        mLocMng.delegate = nil
        mLocMng.stopUpdatingLocation()
    }
}



extension UNUserNotificationCenter {
    func decreaseBadgeCount(by notificationsRemoved: Int? = nil) {
        let notificationsRemoved = notificationsRemoved ?? 1
        DispatchQueue.main.async {
            UIApplication.shared.applicationIconBadgeNumber -= notificationsRemoved
        }
    }

    func removeNotifications(_ notifications: [UNNotification], decreaseBadgeCount: Bool = false) {
        let identifiers = notifications.map { $0.request.identifier }
        UNUserNotificationCenter.current().removeDeliveredNotifications(withIdentifiers: identifiers)
        if decreaseBadgeCount {
            self.decreaseBadgeCount(by: notifications.count)
        }
    }

    func removeNotifications<T: Comparable>(whereKey key: String, hasValue value: T, decreaseBadgeCount: Bool = false) {
        UNUserNotificationCenter.current().getDeliveredNotifications { notifications in
            let notificationsToRemove = notifications.filter {
                guard let userInfoValue = $0.request.content.userInfo[key] as? T else {
                    print(key, " ", $0.request.content.userInfo[key] as! String)
                    return false
                }
                return userInfoValue == value
            }
            self.removeNotifications(notificationsToRemove, decreaseBadgeCount: decreaseBadgeCount)
        }
    }

    func removeNotifications(withThreadIdentifier threadIdentifier: String, decreaseBadgeCount: Bool = false) {
        UNUserNotificationCenter.current().getDeliveredNotifications { notifications in
            let notificationsToRemove = notifications.filter { $0.request.content.threadIdentifier == threadIdentifier }
            self.removeNotifications(notificationsToRemove, decreaseBadgeCount: decreaseBadgeCount)
        }
    }

    func removeNotification(_ notification: UNNotification, decreaseBadgeCount: Bool = false) {
        removeNotifications([notification], decreaseBadgeCount: decreaseBadgeCount)
    }
}

extension String {
    private func matches(pattern: String) -> Bool {
        let regex = try! NSRegularExpression(
            pattern: pattern,
            options: [.caseInsensitive])
        return regex.firstMatch(
            in: self,
            options: [],
            range: NSRange(location: 0, length: utf16.count)) != nil
    }

    func isValidURL() -> Bool {
        guard let url = URL(string: self) else { return false }
        if !UIApplication.shared.canOpenURL(url) {
            return false
        }

        let urlPattern = "^(http|https|ftp)\\://([a-zA-Z0-9\\.\\-]+(\\:[a-zA-Z0-9\\.&amp;%\\$\\-]+)*@)*((25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9])\\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[0-9])|localhost|([a-zA-Z0-9\\-]+\\.)*[a-zA-Z0-9\\-]+\\.(com|edu|gov|int|mil|net|org|biz|arpa|info|name|pro|aero|coop|museum|[a-zA-Z]{2}))(\\:[0-9]+)*(/($|[a-zA-Z0-9\\.\\,\\?\\'\\\\\\+&amp;%\\$#\\=~_\\-]+))*$"
        return self.matches(pattern: urlPattern)
    }
}


