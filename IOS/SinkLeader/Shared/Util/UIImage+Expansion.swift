//
//  ImageUtil.swift
//  
//
//  Created by Snow on 8/7/16.
//  Copyright 2016 Dragon C. All rights reserved.
//

import UIKit

extension UIImage {
    
    static func resize(image: UIImage, newWidth: CGFloat) -> UIImage? {
        let scale = newWidth / image.size.width
        let newHeight = image.size.height * scale
        UIGraphicsBeginImageContext(CGSize(width: newWidth, height: newHeight))
        image.draw(in: CGRect(x: 0, y: 0, width: newWidth, height: newHeight))
        let newImage = UIGraphicsGetImageFromCurrentImageContext()
        UIGraphicsEndImageContext()
        
        return newImage
    }
    
    // from: https://gist.github.com/lynfogeek/4b6ce0117fb0acdabe229f6d8759a139
    
    // colorize image with given tint color
    // this is similar to Photoshop's "Color" layer blend mode
    // this is perfect for non-greyscale source images, and images that have both highlights and shadows that should be preserved
    // white will stay white and black will stay black as the lightness of the image is preserved
    func tint(_ tintColor: UIColor) -> UIImage {
        return modifiedImage { context, rect in
            // draw black background - workaround to preserve color of partially transparent pixels
            context.setBlendMode(.normal)
            UIColor.black.setFill()
            context.fill(rect)
            
            // draw original image
            context.setBlendMode(.normal)
            context.draw(self.cgImage!, in: rect)
            
            // tint image (loosing alpha) - the luminosity of the original image is preserved
            context.setBlendMode(.hardLight)
            tintColor.setFill()
            context.fill(rect)
            
            // mask by alpha values of original image
            context.setBlendMode(.destinationIn)
            context.draw(self.cgImage!, in: rect)
        }
    }
    
    // fills the alpha channel of the source image with the given color
    // any color information except to the alpha channel will be ignored
    func fillAlpha(_ fillColor: UIColor) -> UIImage {
        return modifiedImage { context, rect in
            // draw tint color
            context.setBlendMode(.normal)
            fillColor.setFill()
            context.fill(rect)
            //context.fillCGContextFillRect(context, rect)
            
            // mask by alpha values of original image
            context.setBlendMode(.destinationIn)
            context.draw(self.cgImage!, in: rect)
        }
    }
    
    private func modifiedImage(draw: (CGContext, CGRect) -> ()) -> UIImage {
        // using scale correctly preserves retina images
        UIGraphicsBeginImageContextWithOptions(size, false, scale)
        let context: CGContext! = UIGraphicsGetCurrentContext()
        assert(context != nil)
        
        // correctly rotate image
        context.translateBy(x: 0, y: size.height)
        context.scaleBy(x: 1.0, y: -1.0)
        
        let rect = CGRect(x: 0.0, y: 0.0, width: size.width, height: size.height)
        
        draw(context, rect)
        
        let image = UIGraphicsGetImageFromCurrentImageContext()
        UIGraphicsEndImageContext()
        return image!
    }
    
    func imageBlackAndWhite() -> UIImage? {
		if let beginImage = CoreImage.CIImage(image: self) {
			let paramsColor = [kCIInputBrightnessKey: 0,
			                   kCIInputContrastKey:   1.1,
			                   kCIInputSaturationKey: 0]
            let blackAndWhite = beginImage.applyingFilter("CIColorControls", parameters: paramsColor)
			
			let paramsExposure = [kCIInputEVKey: 0.7]
            let output = blackAndWhite.applyingFilter("CIExposureAdjust", parameters: paramsExposure)
			
			let processedCGImage = CIContext().createCGImage(output, from: output.extent)
			return UIImage(cgImage: processedCGImage!, scale: self.scale, orientation: self.imageOrientation)
		}
		
		return nil
    }
	
	//. hue: [0, 1], saturation: [0, 1], brightness: [0, 1]
	func HSBFilter(hue: CGFloat = 0.5, saturation: CGFloat = 0.5, brightness: CGFloat = 0.5) -> UIImage {
		let context = CIContext(options: nil)
		let inputImage = CIImage(image: self)
		
		let pHue = [kCIInputAngleKey: (2 * hue - 1) * CGFloat.pi]
        var filter = inputImage!.applyingFilter("CIHueAdjust", parameters: pHue)
		
		let pColor = [kCIInputSaturationKey: 2 * saturation, kCIInputBrightnessKey: 2 * brightness - 1]
        filter = filter.applyingFilter("CIColorControls", parameters: pColor)
		
		let render = context.createCGImage(filter, from: filter.extent)
		return UIImage(cgImage: render!)
	}
	
	//. brightness: [0, 1] r, g, b: [0, 1]
	func RGBFilter(brightness: CGFloat = 0.5, r: CGFloat = 0.5, g: CGFloat = 0.5, b: CGFloat = 0.5) -> UIImage {
		let context = CIContext(options: nil)
		let inputImage = CIImage(image: self)
		
		let pColor = [kCIInputBrightnessKey: 2 * brightness - 1]
        var filter = inputImage!.applyingFilter("CIColorControls", parameters: pColor)
		
		let rVec = CIVector(x: pow(100, r - 0.5), y: 0, z: 0, w: 0)
		let gVec = CIVector(x: 0, y: pow(100, g - 0.5), z: 0, w: 0)
		let bVec = CIVector(x: 0, y: 0, z: pow(100, b - 0.5), w: 0)
		//let aVec = CIVector(x: 0, y: 0, z: 0, w: 1)
		//let biasVec = CIVector(x: 0, y: 0, z: 0, w: 0)
		let pRGB = [
			"inputRVector": rVec,
			"inputGVector": gVec,
			"inputBVector": bVec,
			//"inputAVector": aVec,
			//"inputBiasVector": biasVec,
		]
        filter = filter.applyingFilter("CIColorMatrix", parameters: pRGB)
		
		let render = context.createCGImage(filter, from: filter.extent)
		return UIImage(cgImage: render!)
	}
	
	func rotateByAngle(image: UIImage, anlge: CGFloat) -> UIImage {
		//Calculate the size of the rotated view's containing box for our drawing space
		let rotatedViewBox = UIView(frame: CGRect(x: 0, y: 0, width: image.size.width, height: image.size.height))
		rotatedViewBox.transform = CGAffineTransform(rotationAngle: anlge)
		let rotatedSize: CGSize = rotatedViewBox.frame.size
		
		//Create the bitmap context
		UIGraphicsBeginImageContext(rotatedSize)
		let bitmap = UIGraphicsGetCurrentContext()!
		//Move the origin to the middle of the image so we will rotate and scale around the center.
		bitmap.translateBy(x: rotatedSize.width / 2, y: rotatedSize.height / 2)
		//Rotate the image context
		bitmap.rotate(by: anlge)
		//Now, draw the rotated/scaled image into the context
		bitmap.scaleBy(x: 1.0, y: -1.0)
		bitmap.draw(image.cgImage!, in: CGRect(x: -image.size.width / 2, y: -image.size.height / 2,
		                                       width: image.size.width, height: image.size.height))
		let newImage: UIImage = UIGraphicsGetImageFromCurrentImageContext()!
		UIGraphicsEndImageContext()
		
		return newImage
	}
	
	func rotateByDegree(image: UIImage, degree: CGFloat) -> UIImage {
		return rotateByAngle(image: image, anlge: degree * CGFloat.pi / 180)
	}
	
	static func snapshot(_ view: UIView) -> Data {
		UIGraphicsBeginImageContextWithOptions(view.bounds.size, false, 0)
		view.layer.render(in: UIGraphicsGetCurrentContext()!)
		let image = UIGraphicsGetImageFromCurrentImageContext()
		UIGraphicsEndImageContext()
        return image!.jpegData(compressionQuality: 0.8)!
	}
	
    func fixedOrientation() -> UIImage {
        if imageOrientation == .up { return self }
        
        var transform = CGAffineTransform.identity
        
        switch imageOrientation {
        case .down, .downMirrored:
            transform = transform.translatedBy(x: size.width, y: size.height)
            transform = transform.rotated(by: CGFloat.pi)
            
        case .left, .leftMirrored:
            transform = transform.translatedBy(x: size.width, y: 0)
            transform = transform.rotated(by: CGFloat.pi / 2.0)
            
        case .right, .rightMirrored:
            transform = transform.translatedBy(x: 0, y: size.height)
            transform = transform.rotated(by: CGFloat.pi / -2.0)
            
        case .up, .upMirrored:
            break
        }
        
        switch imageOrientation {
        case .upMirrored, .downMirrored:
            transform.translatedBy(x: size.width, y: 0)
            transform.scaledBy(x: -1, y: 1)
            
        case .leftMirrored, .rightMirrored:
            transform.translatedBy(x: size.height, y: 0)
            transform.scaledBy(x: -1, y: 1)
            
        case .up, .down, .left, .right:
            break
        }
        
        let ctx = CGContext(data: nil, width: Int(size.width), height: Int(size.height), bitsPerComponent: self.cgImage!.bitsPerComponent, bytesPerRow: 0, space: self.cgImage!.colorSpace!, bitmapInfo: CGImageAlphaInfo.premultipliedLast.rawValue)!
        ctx.concatenate(transform)
        
        switch imageOrientation {
        case .left, .leftMirrored, .right, .rightMirrored:
            ctx.draw(self.cgImage!, in: CGRect(x: 0, y: 0, width: size.height, height: size.width))
            
        default:
            ctx.draw(self.cgImage!, in: CGRect(x: 0, y: 0, width: size.width, height: size.height))
        }
        
        return UIImage(cgImage: ctx.makeImage()!)
    }
    
}
