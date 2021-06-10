//
//  ViewDelegate.swift
//  AllBaBo
//
//  Created by Aend on 2020/10/22.
//  Copyright © 2020 AllBaBo. All rights reserved.
//

import Foundation

@objc protocol ViewDelegate: class {
    //action : 동작 예약어
    func showMessage(title : String, message : String, action : String)
}
