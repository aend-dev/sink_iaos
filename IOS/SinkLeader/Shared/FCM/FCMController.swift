//
//  FCMController.swift
//  Aend
//
//  Created by mac on 02/12/2019.
//  Copyright © 2019 mac. All rights reserved.
//

import UIKit
import Firebase

class FCMController{
    static let instance = FCMController()
    var delegate : AppDelegate?
    var server_URL : String = ""
    
    init() {}
    
    func setdelegate(_ del:AppDelegate){
        delegate = del
    }
    
    func setServerURL(_ url:String){
        server_URL = url
    }
    
    func registerNoti(_ application: UIApplication) {
        if #available(iOS 10.0, *) {
            // For iOS 10 display notification (sent via APNS)
             UNUserNotificationCenter.current().delegate = delegate
            
            let authOptions: UNAuthorizationOptions = [.alert, .badge, .sound]
            UNUserNotificationCenter.current().requestAuthorization(
                options: authOptions,
                completionHandler: {_, _ in })
            
            // For iOS 10 data message (sent via FCM)
            Messaging.messaging().delegate = delegate
        } else {
            let settings: UIUserNotificationSettings =
                UIUserNotificationSettings(types: [.alert, .badge, .sound], categories: nil)
            application.registerUserNotificationSettings(settings)
        }
        
        application.registerForRemoteNotifications()
        NotificationCenter.default.addObserver(self, selector: #selector(refreshToken), name: NSNotification.Name.MessagingRegistrationTokenRefreshed, object: nil)
    }
    
    func disconnectFcm() {
        print("Disconnected from FCM.")
    }
    
    @objc func refreshToken(_ notification: NSNotification) {
        let token = Messaging.messaging().fcmToken
        print("FCM token: \(String(describing: token))")
        connectFcm()
        delegate?.fcm_token = token!
    }
    
    func connectFcm() {
        print("Connected to FCM.")
    }
    /////////////////////////////////////////////////////////////////////
    // MARK: - push checktoken
    //////////////////////////////////////////////////////////////////////
    func sendRegistrationToServer() -> String{
        let token = Messaging.messaging().fcmToken ?? ""
        
        
//        let uuid = CommonUtil.deviceUUID()
//        Net.checkToken(token: "", uuid: uuid!, fcm_token: token!, app_ver: "1.0", success: { (result) in
//            print("sendRegistrationToServer result : ", result)
//
//        }) { (code, msg) in
//
//        }
        return token
    }
    
    /////////////////////////////////////////////////////////////////////
    // MARK: - push 알림 선택시 데이터 정리 부분 // 사용 안함
    //////////////////////////////////////////////////////////////////////
    
    func phasePush(_ info: [AnyHashable : Any]) {
        print("phasePush : " ,info)
        
//        if let url_str = (info["gcm.notification.url"] as? String){
//            print(WebUrl_Url + url_str)
//            delegate!.enterURL = url_str
//            customVC?.gotoIntro(url: enterURL)
//        }
    }
}
