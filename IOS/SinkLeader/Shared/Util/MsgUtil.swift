//
//  MsgUtil.swift
//  Sun114
//
//  Created by Aend on 2020/08/18.
//  Copyright © 2020 Sun114. All rights reserved.
//

import UIKit

class MsgUtil {

    public typealias callback = () -> ()
    
    static func create(_ msg: String, title: String! = nil, ok: String = "닫기", cancel: String! = nil,
                     okCallback: callback! = nil, cancelCallback: callback! = nil) -> UIAlertController {
        let acAlert = UIAlertController(title: title, message: msg, preferredStyle: .alert)
        if cancel == nil {
            acAlert.addAction(UIAlertAction(title: ok, style: .cancel, handler: { (action: UIAlertAction!) in
                if okCallback != nil {
                    okCallback()
                }
            }))
        } else {
            acAlert.addAction(UIAlertAction(title: cancel, style: .cancel, handler: { (action: UIAlertAction!) in
                if cancelCallback != nil {
                    cancelCallback()
                }
            }))
            acAlert.addAction(UIAlertAction(title: ok, style: .default, handler: { (action: UIAlertAction!) in
                if okCallback != nil {
                    okCallback()
                }
            }))
        }
        
        return acAlert
//        (UIApplication.shared.delegate as! AppDelegate).window?.rootViewController!.present(acAlert, animated: true, completion: nil)
    }
    
    static func show(_ msg: String, title: String! = nil, ok: String = "닫기", cancel: String! = nil,
                     okCallback: callback! = nil, cancelCallback: callback! = nil) {
        let acAlert = UIAlertController(title: title, message: msg, preferredStyle: .alert)
        if cancel == nil {
            acAlert.addAction(UIAlertAction(title: ok, style: .cancel, handler: { (action: UIAlertAction!) in
                if okCallback != nil {
                    okCallback()
                }
            }))
        } else {
            acAlert.addAction(UIAlertAction(title: cancel, style: .cancel, handler: { (action: UIAlertAction!) in
                if cancelCallback != nil {
                    cancelCallback()
                }
            }))
            acAlert.addAction(UIAlertAction(title: ok, style: .default, handler: { (action: UIAlertAction!) in
                if okCallback != nil {
                    okCallback()
                }
            }))
        }
        (UIApplication.shared.delegate as! AppDelegate).window?.rootViewController!.present(acAlert, animated: true, completion: nil)
    }
    
}

