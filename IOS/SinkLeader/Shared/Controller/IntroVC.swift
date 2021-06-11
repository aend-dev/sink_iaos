//
//  IntroVC.swift
//  AllBaBo
//
//  Created by Aend on 2020/10/15.
//  Copyright © 2020 AllBaBo. All rights reserved.
//

import UIKit
import WebKit
import Foundation

class IntroVC: BaseVC {
    var mTimer : Timer?
    var mTimeNum : Int = 0
    
    var mUrl = ""
    var reviewURL : URL!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        Net.changeServer(2)
        
        WKWebsiteDataStore.default().fetchDataRecords(ofTypes: WKWebsiteDataStore.allWebsiteDataTypes(), completionHandler: {
            (records) -> Void in
            for record in records{
                WKWebsiteDataStore.default().removeData(ofTypes: record.dataTypes, for: [record], completionHandler: {})
               //remove callback
            }
        })
        
        mTimer = Timer.scheduledTimer(withTimeInterval: 0.5, repeats: true){ timer in
            print(self.mTimeNum)

            if self.mTimeNum == 2{
                self.mTimer!.invalidate()
                self.requestCheckVersion()
            }
            self.mTimeNum += 1
        }
    }
    
    func openLogin(){
        let vc = UIStoryboard(name: "Main", bundle: nil).instantiateViewController(withIdentifier: "LOGIN_VC") as! LoginVC
        self.present(vc, animated: true)
    }
    
    func openWebview(){
        let vc = UIStoryboard(name: "Main", bundle: nil).instantiateViewController(withIdentifier: "WEB_VC") as! WebVC
        if mUrl.count == 0 {
            vc.mUrl = FrontList[Net.FrontIndex] + "/home"
        }else{
            vc.mUrl = mUrl
        }
        
        self.present(vc, animated: true)
    }
}


extension IntroVC {
    func requestCheckVersion(){
        let url = ServerList[Net.Index] + "/v1/main/app-version"
        
        let params = [:] as [String:Any]
        
        Net.doRequest(method: .get, api: url, params: params, success: {(result) in
            print("requestCheckVersion result : ", result)
            let code = result!["result"].numberValue
            
            if code == 0 {
                let data = result!["data"]
                let list = data["list"].arrayValue

                var ver = ""
                var msg = ""
                var upd_type = ""
                
                for i in 0..<list.count {
                    let item = list[i]
                    let os = item["os"].stringValue
                    
                    if os.elementsEqual("IOS") {
                        ver = item["vesion"].stringValue
                        msg = item["message"].stringValue
                        upd_type = item["upd_force_yn"].stringValue
//                        self.reviewURL = URL(string: "itms-apps://itunes.apple.com/app/apple-store/431589174")
                        self.reviewURL = URL(fileURLWithPath: item["url"].stringValue)
                    }
                }
                
//                ver = "1.1.0"
//                upd_type = "C"
                
                if !self.enterMarket(checkver: ver, message: msg, upd_type: upd_type) {
                    if self.m_appDelegate.token.elementsEqual("") {
                        self.openLogin()
                    }else{
                        self.requestAutoLOGIN()
                    }
                }
                
            }else{
                let msg = result!["message"].stringValue
                self.showPopup(dlg: MessagePopup(msg))
            }
            
        }, failure: {(code, msg) in
            print(msg)
            
            self.mTimer = Timer.scheduledTimer(withTimeInterval: 0.5, repeats: true){ timer in
                print(self.mTimeNum)

                if self.mTimeNum > 5{
                    self.mTimeNum = 0
                    self.mTimer!.invalidate()
                    self.requestCheckVersion()
                }
                self.mTimeNum += 1
            }
        })
    }
    
    func requestAutoLOGIN(){
        var param : [String:Any] = ["userDevice":self.getDeviceInfo(),
                                    "x_token":m_appDelegate.token]
        let url = ServerList[Net.Index] + "/v1/login/autoLogin"
        
        Net.doRequestJson(method: .post, api: url, params: param, success: {result in
            print("requestAutoLOGIN Suc : \(result)")
            let code = result!["result"].numberValue

            if code == 0 {
                let data = result!["data"]
                let user = data["user"]
                
                self.m_appDelegate.token = user["token"].stringValue
                self.m_appDelegate.RETOKEN = user["refresh_token"].stringValue
                self.m_appDelegate.USER_SEQ = user["user_seq"].stringValue
                self.m_appDelegate.USER_NAME = user["name"].stringValue
                self.m_appDelegate.USER_IMG = user["profile_img_url"].stringValue
                self.m_appDelegate.USER_LASTDATE = user["last_access_date"].stringValue

               if self.m_appDelegate.token.elementsEqual("") {
                    self.openLogin()
               }else{
                    self.openWebview()
               }
           }else{
               self.openLogin()
           }
       }, failure: {(code, msg) in
           print("requestSNSLOGIN Err : \(code) \(msg)")
           self.openLogin()
       })
   }
    
    
    func enterMarket(checkver:String, message:String, upd_type:String) -> Bool{
        if checkver.count < 5{
            return false
        }
        
        let curStrVers = CommonUtil.bundleVer().components(separatedBy: ".")
        let curVer = UInt(String(format: "%04d%04d%04d",UInt(curStrVers[0])!,UInt(curStrVers[1])!,UInt(curStrVers[2])!))!
        
        let serStrVers = checkver.components(separatedBy: ".")
        let serVer = UInt(String(format: "%04d%04d%04d",UInt(serStrVers[0])!,UInt(serStrVers[1])!,UInt(serStrVers[2])!))!
        
        if curVer < serVer
        {
            if upd_type.elementsEqual("Y") {
                showPopup(dlg: MessagePopup(message, self.moveMarket))
                return true
            }else if upd_type.elementsEqual("C"){
                DialogPopup2.show(self, message, self.moveMarket, {
                    if self.m_appDelegate.token.elementsEqual("") {
                        self.openLogin()
                    }else{
                        self.requestAutoLOGIN()
                    }
                })
                return true
            }
        }
        return false
    }
    
    func moveMarket() {
        if UIApplication.shared.canOpenURL(reviewURL) {
            UIApplication.shared.open(reviewURL, options: [:], completionHandler: nil)
        } else {
            print("can't open app store url")
        }
    }
}
