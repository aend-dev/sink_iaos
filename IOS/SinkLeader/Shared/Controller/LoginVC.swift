//
//  LoginVC.swift
//  AllBaBo
//
//  Created by Aend on 2020/10/15.
//  Copyright © 2020 AllBaBo. All rights reserved.
//

import UIKit
import Firebase
import SwiftyJSON

import Toast_Swift
import Alamofire
import WebKit

class LoginVC: BaseVC{
    @IBOutlet weak var vFindPass: UIView!
    
    @IBOutlet weak var lPhoneNum: UILabel!
    @IBOutlet weak var imgPhone: UIImageView!
    
    @IBOutlet weak var tfID: UITextField!
    @IBOutlet weak var tfPass: UITextField!
    
    @IBOutlet weak var swID: UISwitch!
    @IBOutlet weak var swAuto: UISwitch!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        initView()
        
        var locals = WKWebsiteDataTypeLocalStorage
        locals.removeAll()
    }
    
    func initView() {
        if !m_appDelegate.userID.elementsEqual("") {
            swID.isOn = true
            tfID.text = m_appDelegate.userID
        }
        
        //ID View setting
        let find_gesture = UITapGestureRecognizer(target: self, action: #selector(self.openFindPass))
        vFindPass.isUserInteractionEnabled = true
        vFindPass.addGestureRecognizer(find_gesture)
        
        let num_gesture = UITapGestureRecognizer(target: self, action: #selector(self.openTEST))
        lPhoneNum.isUserInteractionEnabled = true
        lPhoneNum.addGestureRecognizer(num_gesture)
        let img_gesture = UITapGestureRecognizer(target: self, action: #selector(self.openTel))
        imgPhone.isUserInteractionEnabled = true
        imgPhone.addGestureRecognizer(img_gesture)
    }
}

extension LoginVC : Actions{
    @objc func openFindPass(){
        let vc = UIStoryboard(name: "Main", bundle: nil).instantiateViewController(withIdentifier: "FIND_VC") as! FindVC
        self.present(vc, animated: true)
    }
    
    @objc func openTel(){
        let callnum =  "1688-7992"

        if let phoneURL = URL(string: "tel://" + callnum){
            let app : UIApplication = UIApplication.shared

            if(app.canOpenURL(phoneURL)){
                app.open(phoneURL, options: [:])
            }
        }
    }
    
    @objc func openTEST(){
        let vc = UIStoryboard(name: "Main", bundle: nil).instantiateViewController(withIdentifier: "SIGN_VC") as! SignatureVC
//        vc.backString =
//        vc.cbFunction =
        self.present(vc, animated: true)
    }

    @IBAction func requestLOGIN(_ sender: Any){
        print("requestLOGIN Start :")
        var errTxt = ""
        
        let id = tfID.text
        let pass = tfPass.text
        
        if id?.elementsEqual("") ?? true{
            errTxt = "아이디를 입력하세요."
        }else if pass?.elementsEqual("") ?? true{
            errTxt = "비밀번호를 입력하세요."
        }
        
        if errTxt.count > 0{
            showPopup(dlg: MessagePopup(errTxt))
            return
        }
        
        let userDevice = getDeviceInfo()
        
        let params = ["user_id": id!,
                      "password":pass!,
                      "userDevice": userDevice] as [String : Any]
        
        let url = ServerList[Net.Index] + "/v1/login/login"
        
        print("Login url : ", url, ", param : ", params)
    
        Net.doRequestJson(method: .post, api: url, params: params, success: {result in
            print("requestLOGIN Suc : \(result)")
            let code = result!["result"].numberValue
            if code == 0 {
                let data = result!["data"]
                let user = data["user"]
                
                if self.swID.isOn {
                    self.m_appDelegate.userID = id!
                }else{
                    self.m_appDelegate.userID = ""
                }
                
                self.m_appDelegate.isAutoLogin = self.swAuto.isOn
                
                self.m_appDelegate.token = user["token"].stringValue
                self.m_appDelegate.RETOKEN = user["refresh_token"].stringValue
                self.m_appDelegate.USER_NAME = user["user_name"].stringValue
                self.m_appDelegate.USER_SEQ = user["user_seq"].stringValue
                self.m_appDelegate.USER_LASTDATE = user["last_access_date"].stringValue
                self.m_appDelegate.USER_IMG = user["profile_img_url"].stringValue
                self.m_appDelegate.CENTER_SEQ = user["center_seq"].stringValue
                self.m_appDelegate.USER_TYPE = user["user_type"].stringValue
                self.m_appDelegate.USER_GRADE = user["app_user_grade"].stringValue
                
                let vc = UIStoryboard(name: "Main", bundle: nil).instantiateViewController(withIdentifier: "WEB_VC") as! WebVC
                vc.mUrl = FrontList[Net.FrontIndex] + "/home"
                self.present(vc, animated: true)
                
            }else{
                let msg = result!["message"].stringValue
                self.showPopup(dlg: MessagePopup(msg))
            }
       }, failure: {(code, msg) in
           print("requestLOGIN Err : \(code) \(msg)")
       })
   }
}

