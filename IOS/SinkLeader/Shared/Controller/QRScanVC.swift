//
//  FindVC.swift
//  AllBaBo
//
//  Created by Aend on 2020/10/15.
//  Copyright © 2020 AllBaBo. All rights reserved.
//

import UIKit

class QRScanVC: BaseVC {
    @IBOutlet weak var vScanner: QRScannerView!{
        didSet {
            vScanner.delegate = self
        }
    }
    
    @IBOutlet var QRtitle : UILabel!
    
    var name : String = "1차기 제품 S/N"
    var method : String = ""
    var cbFunction: callback!
    
    
    public typealias callback = (String, String, Bool) -> ()
    
    override func viewDidLoad() {
        QRtitle.text = name
    }
    
    
    override func viewWillAppear(_ animated: Bool) {
       super.viewWillAppear(animated)

       if !vScanner.isRunning {
        vScanner.startScanning()
       }
    }

    override func viewWillDisappear(_ animated: Bool) {
       super.viewWillDisappear(animated)
       if !vScanner.isRunning {
        vScanner.stopScanning()
       }
    }
}

extension QRScanVC : Actions {
    @IBAction func ActionBack(_ sender: Any) {
        dismiss(animated: true)
    }
    
    
    @IBAction func ActionOpenDirect(_ sender: Any) {
        ScanPopup.show(self,title: name, responseQRcode)
        
    }
    
    func responseQRcode(_ code:String,_ isScan:Bool) {
        print(code)        
        if cbFunction != nil {
            dismiss(animated: true){
                self.cbFunction(self.method, code, isScan)
            }
        }
    }
}


extension QRScanVC: QRScannerViewDelegate {
    func qrScanningDidStop() {
//        let buttonTitle = scannerView.isRunning ? "STOP" : "SCAN"
//        scanButton.setTitle(buttonTitle, for: .normal)
    }
    
    func qrScanningDidFail() {
        print("Scanning Failed. Please try again")
        showToast(message: "Scanning Failed. Please try again")
    }
    
    func qrScanningSucceededWithCode(_ str: String?) {
//        self.qrData = QRData(codeString: str)
        
        responseQRcode(str!, true)
    }
}



