//
//  BaseVC.swift
//  AllBaBo
//
//  Created by Aend on 2020/10/15.
//  Copyright © 2020 AllBaBo. All rights reserved.
//

import UIKit
import CommonCrypto

class BaseVC: UIViewController {
    var m_appDelegate : AppDelegate!
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        m_appDelegate = UIApplication.shared.delegate as? AppDelegate
        m_appDelegate.customVC = self
    }
    
    override func viewWillAppear(_ animated: Bool) {
    }
    
    func gotoIntro(url: String){
        let vc = UIStoryboard(name: "Main", bundle: nil).instantiateViewController(withIdentifier: "INTRO_VC") as! IntroVC
        vc.mUrl = url
        self.present(vc, animated: false)
    }
    
    @objc public func onTopBack(_ sender: UIButton) {
        // 타이틀 바2의 뒤로가기 버튼
        processOnTopBack()
    }
    
    func processOnTopBack() {
        
    }
    
    override func touchesBegan(_ touches: Set<UITouch>, with event: UIEvent?){
        self.view.endEditing(true)
    }
    
    @objc public func onTopNext(_ sender: UIButton) {
        // 타이틀 바2의 다음 버튼
        processOnTopNext()
    }
    
    func processOnTopNext() {
        
    }
    
    func replaceVC(identifier : String, storyboard : String, animated : Bool) {
        let nav : UINavigationController! = self.navigationController
        let storyboard : UIStoryboard! = UIStoryboard(name: storyboard, bundle: nil)
        let vc = (storyboard.instantiateViewController(withIdentifier: identifier))
        nav.setViewControllers([vc], animated: animated)
    }
    
    func pushVC(identifier : String, storyboard : String, animated : Bool) {
        let nav : UINavigationController! = self.navigationController
        let storyboard : UIStoryboard! = UIStoryboard(name: storyboard, bundle: nil)
        let vc = (storyboard.instantiateViewController(withIdentifier: identifier))
        nav.pushViewController(vc, animated: animated)
    }
    
    func popVC(backStep : Int32 = -1) {
        let nav : UINavigationController! = self.navigationController
        var viewVCs : [UIViewController] = nav.viewControllers
        for _ in 1...(0 - backStep) {
            viewVCs.removeLast()
        }
        nav.setViewControllers(viewVCs, animated: true)
    }
    
    func popVCToRoot(backStep : Int32 = -1) {
        let nav : UINavigationController! = self.navigationController
        nav.setViewControllers([nav.viewControllers[0]], animated: true)
    }
    
    func presentVC(identifier : String, storyboard : String, animated : Bool) {
        let storyboard : UIStoryboard! = UIStoryboard(name: storyboard, bundle: nil)
        let vc = (storyboard.instantiateViewController(withIdentifier: identifier))
        vc.modalTransitionStyle = UIModalTransitionStyle.crossDissolve
        self.present(vc, animated: animated, completion: nil)
    }
    
    func showPopup(dlg : UIViewController) {
        dlg.modalPresentationStyle = .overCurrentContext
        dlg.modalTransitionStyle = UIModalTransitionStyle.crossDissolve
        self.present(dlg, animated: true, completion: nil)
    }
    
    func getDeviceInfo() -> [String:Any] {
        let fcm = m_appDelegate.fcm_token

        let result = [
            "agent": Agent,
            "dev_model": CommonUtil.modelName(),
            "os": "IOS",
            "os_version": CommonUtil.systemVer(), //??
            "dev_ip" : getIPAddress()!,
            "dev_token": fcm,// fcm
        ] as [String:Any]
        
        return result
    }
    
    @objc func onLockPage(){
//        if m_appDelegate.lock {
//            let vc = UIStoryboard(name: "LaunchScreen", bundle: nil).instantiateViewController(withIdentifier: "LOCK_VC") as! LockViewController
//            self.present(vc, animated: true)
//        }
    }
    
//    func encrypt(string:String) -> String {
//        var result = ""
//        let key = "sun114_aend_encodeaes_ksy_sun114"
//        let iv = [0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00] as [UInt8]
//
//        let et = try! string.encryptToBase64(cipher: AES(key: key.bytes, blockMode: CBC(iv: iv)))
//        
//        result = et!;
//        
//        return result
//    }
    
    func makeFormattedText(str : NSString) -> NSAttributedString {
        if (str == "") {
            return NSAttributedString(string: "")
        }
        
        let attr_str = NSMutableAttributedString()
        let textScanner = Scanner(string: String(str))
        
        let colorUser = UIColor(red: 0xff/255.0, green: 0xbc/255.0, blue: 0x33/255.0, alpha: 1.0)
        let colorTag = UIColor(red: 0x6e/255.0, green: 0xa6/255.0, blue: 0xe8/255.0, alpha: 1.0)
        
        let charEndSet : CharacterSet = CharacterSet.whitespacesAndNewlines
        
        var curStr: NSString?
        
        var lastLocation = 0
        
        var strWord : String = ""
        
        lastLocation = textScanner.scanLocation
        
        while(textScanner.scanUpToCharacters(from: charEndSet, into: &curStr)) {
            strWord = curStr! as String
            
            if (lastLocation + curStr!.length < textScanner.scanLocation) {
                let len = textScanner.scanLocation - lastLocation - curStr!.length
                let strSpaceLine = str.substring(with: NSRange(location: lastLocation, length: len))
                attr_str.append(NSAttributedString(string: strSpaceLine))
            }
            
            if (strWord.starts(with: "@")) {
                attr_str.append(NSAttributedString(string: strWord, attributes: [NSAttributedString.Key.foregroundColor : colorUser]))
            } else if (strWord.starts(with: "#")) {
                attr_str.append(NSAttributedString(string: strWord, attributes: [NSAttributedString.Key.foregroundColor : colorTag]))
            } else {
                attr_str.append(NSAttributedString(string: strWord))
            }
            
            lastLocation = textScanner.scanLocation
        }
        
        strWord = str.substring(from: textScanner.scanLocation)
        
        if (strWord.starts(with: "@")) {
            attr_str.append(NSAttributedString(string: strWord, attributes: [NSAttributedString.Key.foregroundColor : colorUser]))
        } else if (strWord.starts(with: "#")) {
            attr_str.append(NSAttributedString(string: strWord, attributes: [NSAttributedString.Key.foregroundColor : colorTag]))
        } else {
            attr_str.append(NSAttributedString(string: strWord))
        }
        
        return attr_str
    }
}

extension UIViewController {
    func showToast(message : String) {
        self.view.makeToast(message)
    }
}



extension BaseVC {
    private struct InterfaceNames {
        static let wifi = ["en0"]
        static let wired = ["en2", "en3", "en4"]
        static let cellular = ["pdp_ip0","pdp_ip1","pdp_ip2","pdp_ip3"]
        static let supported = wifi + wired + cellular
    }

    func getIPAddress() -> String? {
        var ipAddress: String?
        var ifaddr: UnsafeMutablePointer<ifaddrs>?

        if getifaddrs(&ifaddr) == 0 {
            var pointer = ifaddr

            while pointer != nil {
                defer { pointer = pointer?.pointee.ifa_next }

                guard
                    let interface = pointer?.pointee,
                    interface.ifa_addr.pointee.sa_family == UInt8(AF_INET) || interface.ifa_addr.pointee.sa_family == UInt8(AF_INET6),
                    let interfaceName = interface.ifa_name,
                    let interfaceNameFormatted = String(cString: interfaceName, encoding: .utf8),
                    InterfaceNames.supported.contains(interfaceNameFormatted)
                    else { continue }

                var hostname = [CChar](repeating: 0, count: Int(NI_MAXHOST))

                getnameinfo(interface.ifa_addr,
                            socklen_t(interface.ifa_addr.pointee.sa_len),
                            &hostname,
                            socklen_t(hostname.count),
                            nil,
                            socklen_t(0),
                            NI_NUMERICHOST)

                guard
                    let formattedIpAddress = String(cString: hostname, encoding: .utf8),
                    !formattedIpAddress.isEmpty
                    else { continue }

                ipAddress = formattedIpAddress
                break
            }
            freeifaddrs(ifaddr)
        }
        return ipAddress ?? "0.0.0.0"
    }
}
