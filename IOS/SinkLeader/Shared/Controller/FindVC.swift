//
//  FindVC.swift
//  AllBaBo
//
//  Created by Aend on 2020/10/15.
//  Copyright © 2020 AllBaBo. All rights reserved.
//

import UIKit

class FindVC: BaseVC {
    @IBOutlet weak var tfID: UITextField!
    @IBOutlet weak var tfPhone: UITextField!
    @IBOutlet weak var tfAuth: UITextField!
    @IBOutlet weak var tfPassword: UITextField!
    
    @IBOutlet weak var vSucAuth: UIView!
    
    @IBOutlet weak var btnRequest: UIButton!
    @IBOutlet weak var btnCheck: UIButton!
    @IBOutlet weak var btnPassword: UIButton!
    
    @IBOutlet weak var lTimer: UILabel!
    
    var mTimer : Timer?
    var smsID : String = ""
    var limitDate : Date?
    
    override func viewDidLoad() {
        initUI()
    }
    
    override func viewWillDisappear(_ animated: Bool) {
        stopTimer()
        super.viewWillDisappear(animated)
    }
    
    func initUI(){        
        let color = UIColor.init(red: 8/255, green: 49/255, blue: 144/255, alpha: 1)
        btnRequest.layer.borderColor = color.cgColor.copy()
        btnRequest.layer.borderWidth = 1
        btnCheck.layer.borderColor = color.cgColor.copy()
        btnCheck.layer.borderWidth = 1
        
        tfAuth.isEnabled = false
        tfPassword.isEnabled = false
        btnCheck.isEnabled = false
        btnPassword.isEnabled = false
    }
    
    func setTimer(time:String) {
        print("setTimer : ", time)
        
        let dataFormat = DateFormatter()
        dataFormat.dateFormat = "yyyy-MM-dd HH:mm:ss"
        dataFormat.timeZone = NSTimeZone(name: "Asia/Seoul") as TimeZone?
        
        let now = Date()
        limitDate = dataFormat.date(from: time)!
        
        print("setTimer", Date(timeIntervalSinceNow: (limitDate?.timeIntervalSince(now))!))
        lTimer.isHidden = false
        
        if let timer = mTimer {
            //timer 객체가 nil 이 아닌경우에는 invalid 상태에만 시작한다
            if !timer.isValid {
                /** 1초마다 timerCallback함수를 호출하는 타이머 */
                mTimer = Timer.scheduledTimer(timeInterval: 1, target: self, selector: #selector(timerCallback), userInfo: nil, repeats: true)
            }
        }else{
            //timer 객체가 nil 인 경우에 객체를 생성하고 타이머를 시작한다
            /** 1초마다 timerCallback함수를 호출하는 타이머 */
            mTimer = Timer.scheduledTimer(timeInterval: 1, target: self, selector: #selector(timerCallback), userInfo: nil, repeats: true)
        }
    }
    
    @objc func timerCallback(){
        let times = limitDate?.timeIntervalSince1970
        
//        times = limit.timeIntervalSince1970
        let now = Date()
        let timeInterval = now.timeIntervalSince1970
        print("limit ", times!, " now ", timeInterval)
        
        let temp = limitDate?.timeIntervalSince(now)
        
        let sssss = Date(timeIntervalSince1970: temp!)
        let dataFormat = DateFormatter()
        dataFormat.dateFormat = "mm:ss"
        dataFormat.timeZone = NSTimeZone(name: "UTC") as TimeZone?
        
        let txt = dataFormat.string(from: sssss)
        print("times : ", txt)
        
        lTimer.text = txt

        if times! < timeInterval {
            stopTimer()
            MessagePopup.show(self, "인증 시간을 초과하였습니다")
            return
        }
    }
    
    func stopTimer(){
        lTimer.isHidden = true
        mTimer?.invalidate()
        mTimer = nil
    }
}

extension FindVC : Actions {
    @IBAction func ActionBack(_ sender: Any) {
        dismiss(animated: true)
    }
    
    @IBAction func ActionRequestAuth(_ sender: Any) {
        var errTxt = ""
        
        let id = tfID.text!
        let tel = tfPhone.text!
        
        if id.elementsEqual("") {
            errTxt = "아이디를 입력하세요."
        }else if tel.elementsEqual(""){
            errTxt = "휴대폰 번호를 입력하세요."
        }
        
        if errTxt.count > 0{
            MessagePopup.show(self, errTxt)
            return
        }
        
        let params = ["auth_type":"SEARCH_PASSWORD",
                      "cell_phone":tel,
                      "user_id":id]
        
        let url = ServerList[Net.Index] + "/v1/common/sms/send"
        
        Net.doRequestJson(method: .post, api: url, params: params, success: {
            result in
            print("RequestAuth Suc : \(result)")
            
            let code = result!["result"].numberValue
            
            if code == 0 {
                let data = result!["data"]
                self.smsID = data["sms_auth_id"].stringValue
                
                let time = data["limit_date"].stringValue
                
                self.setTimer(time: time)
                
                self.tfAuth.isEnabled = true
                self.btnCheck.isEnabled = true
            }else{
                let msg = result!["message"].stringValue
                MessagePopup.show(self, msg)
            }
            
        }, failure: {(code, msg) in
            print("RequestAuth Err : \(code) \(msg)")
        })
    }
    
    @IBAction func ActionCheckAuth(_ sender: Any) {
        var errTxt = ""
        
        let num = tfAuth.text!
        
        if num.elementsEqual("") {
            errTxt = "인증번호를 입력하세요."
        }
        
        if errTxt.count > 0{
            
            MessagePopup.show(self, errTxt)
            return
        }
        
        let params = ["sms_auth_id":smsID,
                      "auth_number":num,
                      "auth_type":"SEARCH_PASSWORD"]
        
        let url = ServerList[Net.Index] + "/v1/common/sms/check"
        
        Net.doRequestJson(method: .put, api: url, params: params, success: {
            result in
            print("CheckAuth Suc : \(result)")
            
            let code = result!["result"].numberValue
            
            if code == 0 {
                let data = result!["data"]

                self.tfID.isEnabled = false
                self.tfPhone.isEnabled = false
                
                self.vSucAuth.isHidden = false
                self.tfPassword.isEnabled = true
                self.btnPassword.isEnabled = true
                self.stopTimer()
            }else{
                let msg = result!["message"].stringValue
                MessagePopup.show(self, msg)
            }
            
        }, failure: {(code, msg) in
            print("CheckAuth Err : \(code) \(msg)")
        })
    }
    
    
    @IBAction func ActionNewPass(_ sender: Any) {
        DialogPopup2.show(self, "비밀번호를 변경하시겠습니까?",{
            self.requestNewPass()
        })
    }
    
    func requestNewPass() {
        var errTxt = ""
        
        let num = tfPassword.text!
        
        if num.elementsEqual("") {
            errTxt = "인증번호를 입력하세요."
        }
        
        if errTxt.count > 0{
            
            MessagePopup.show(self, errTxt)
            return
        }
        
        let params = ["sms_auth_id":smsID,
                      "cell_phone":tfPhone.text!,
                      "password_new":num,
                      "user_id":tfID.text!]
        
        let url = ServerList[Net.Index] + "/v1/user/find/password"
        
        Net.doRequestJson(method: .put, api: url, params: params, success: {
            result in
            print("CheckAuth Suc : \(result)")
            
            let code = result!["result"].numberValue
            
            if code == 0 {
                MessagePopup.show(self, "변경되었습니다.",{
                    self.dismiss(animated: true)
                })
                
            }else{
                let msg = result!["message"].stringValue
                MessagePopup.show(self, msg)
            }
            
        }, failure: {(code, msg) in
            print("CheckAuth Err : \(code) \(msg)")
        })
    }
    
}



