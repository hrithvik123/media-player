import Foundation
import UIKit

public class MediaPlayerPlacementOptions: NSObject {
    var videoOrientation: String
    var horizontalAlignment: String
    var verticalAlignment: String
    
    var height: CGFloat = CGFloat(0)
    var width: CGFloat = CGFloat(0)
    
    var horizontalMargin: CGFloat
    var verticalMargin: CGFloat

    init(height: Float?, width: Float?, videoOrientation: String?, verticalMargin: Float?, horizontalMargin: Float?, horizontalAlignment: String?, verticalAlignment: String?){
        self.videoOrientation = videoOrientation ?? "HORIZONTAL"
        self.horizontalAlignment = horizontalAlignment ?? "CENTER"
        self.verticalAlignment = verticalAlignment ?? "TOP"
        
        self.horizontalMargin = CGFloat(horizontalMargin ?? 0)
        self.verticalMargin = CGFloat(verticalMargin ?? 0)
        
        if height != nil && width != nil {
            self.height = CGFloat(height!)
            self.width = CGFloat(width!)
        } else if height == nil && width == nil {
            if self.videoOrientation == "HORIZONTAL" {
                self.width = UIScreen.main.bounds.width - self.horizontalMargin
                self.height = 9/16 * self.width
            } else {
                self.height = UIScreen.main.bounds.height - self.verticalMargin
                self.width = 9/16 * self.height
            }
        } else if height != nil {
            self.height = CGFloat(height!)
            if self.videoOrientation == "HORIZONTAL" {
                self.width = 16/9 * self.height
            } else {
                self.width = 9/16 * self.height
            }
        } else if width != nil {
            self.width = CGFloat(width!)
            if self.videoOrientation == "HORIZONTAL" {
                self.height = 9/16 * self.width
            } else {
                self.height = 16/9 * self.width
            }
        }

        if (self.width + CGFloat(horizontalMargin ?? 0)) > UIScreen.main.bounds.width {
            self.width = UIScreen.main.bounds.width - self.horizontalMargin
            if self.videoOrientation == "HORIZONTAL" {
                self.height = 9/16 * self.width
            } else {
                self.height = 16/9 * self.width
            }
        }
        
        if (self.height + CGFloat(verticalMargin ?? 0)) > UIScreen.main.bounds.height {
            self.height = UIScreen.main.bounds.height - self.verticalMargin
            if self.videoOrientation == "HORIZONTAL" {
                self.width = 16/9 * self.height
            } else {
                self.width = 9/16 * self.height
            }
        }

    }
}
