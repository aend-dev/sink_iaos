//
//  BackDelegate.swift
//  AllBaBo
//
//  Created by Aend on 2020/10/15.
//  Copyright © 2020 AllBaBo. All rights reserved.
//

import Foundation

@objc protocol BackDelegate: class {
    func callback(_ url : String?,_ data : [String:Any])
}
