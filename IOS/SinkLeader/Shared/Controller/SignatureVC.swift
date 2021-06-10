//
//  FindVC.swift
//  AllBaBo
//
//  Created by Aend on 2020/10/15.
//  Copyright © 2020 AllBaBo. All rights reserved.
//

import UIKit
import SVGKit

class SignatureVC: BaseVC {
    @IBOutlet weak var vSign: DrawSignatureView!
    @IBOutlet weak var vBottomSize: NSLayoutConstraint!
    
    var method : String = ""
    var cbFunction: callback!

    public typealias callback = (String, String) -> ()
    
    override func viewDidLoad() {
        super.viewDidLoad()
    }
    
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        m_appDelegate.isAllOrientation = true

        let bg_color = UIColor.init(red: 250/255, green: 250/255, blue: 250/255, alpha: 1)
        vSign.layer.backgroundColor = bg_color.cgColor.copy()
        let border_color = UIColor.init(red: 160/255, green: 173/255, blue: 186/255, alpha: 1)
        vSign.layer.borderColor = border_color.cgColor.copy()
        vSign.layer.borderWidth = 1
        
        let screenSize = UIScreen.main.bounds
        print(screenSize)
        
        vBottomSize.constant = screenSize.width - 40
    }

    override func viewWillDisappear(_ animated: Bool) {
        m_appDelegate.isAllOrientation = false
        super.viewWillDisappear(animated)
        
    }
}

extension SignatureVC : Actions {
    @IBAction func ActionBack(_ sender: Any) {
        dismiss(animated: true)
    }
    
    
    @IBAction func ActionCompleteSign(_ sender: Any) {
        let image = vSign?.captureSignatureFromView()
        let data = image?.pngData()!
        
        saveSVGFile(strData: data?.base64EncodedString() ?? "", width: (image?.size.width)!, height: (image?.size.height)!)
    }
    
    func saveSVGFile(strData : String, width:CGFloat, height:CGFloat){
        if strData.count == 0 {
            return
        }
        
//        let fileManager = FileManager.default
//        let documentsURL = fileManager.urls(for: .documentDirectory, in: .userDomainMask).first!

        let svgdata = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1//EN\" \"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd\"> <svg version=\"1.1\" id=\"Layer_1\" xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" x=\"0px\" y=\"0px\" width=\""+width.description+"px\" height=\""+height.description+"px\" viewBox=\"0 0 "+width.description+" "+height.description+"\" enable-background=\"new 0 0 "+width.description+" "+height.description+"\" xml:space=\"preserve\">  <image id=\"image0\" width=\""+width.description+"\" height=\""+height.description+"\" x=\"0\" y=\"0\" xlink:href=\"data:image/png;base64,\(strData)\"/> </svg>"
        
//        print(svgdata)

        DispatchQueue(label: "org.alamofire.session-manager." + UUID().uuidString).sync {
            var data64 = svgdata.data(using: String.Encoding.utf8)!.base64EncodedString(options: .lineLength64Characters)
            data64 = data64.replacingOccurrences(of: "\r\n", with: "")
            
            print( "data64 : " , data64)
            
            let uploadStr = "data:image/svg+xml;base64," + data64
            
            dismiss(animated: true, completion: {
                self.cbFunction(self.method, uploadStr)
            })
        }
    }
}
