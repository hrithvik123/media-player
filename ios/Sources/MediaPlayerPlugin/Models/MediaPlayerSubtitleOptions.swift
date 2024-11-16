import Foundation

public class MediaPlayerSubtitleSettings: NSObject {
    var language: String?
    var foregroundColor: String?
    var backgroundColor: String?
    var fontSize: Float?
    
    init(language: String?, foregroundColor: String?, backgroundColor: String?, fontSize: Float?){
        self.language = language
        self.foregroundColor = foregroundColor
        self.backgroundColor = backgroundColor
        self.fontSize = fontSize
    }
}

public class MediaPlayerSubtitleOptions: NSObject {
    var url: URL
    var settings: MediaPlayerSubtitleSettings
    

    init(url: URL, language: String?, foregroundColor: String?, backgroundColor: String?, fontSize: Float?) {
        self.url = url
        self.settings = MediaPlayerSubtitleSettings(language: language, foregroundColor: foregroundColor, backgroundColor: backgroundColor, fontSize: fontSize)
    }
}
