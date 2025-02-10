import Foundation

public class MediaPlayerExtraOptions: NSObject {
    var title: String?
    var subtitle: String?
    var poster: URL?
    var artist: String?
    var rate: Float
    var subtitles: MediaPlayerSubtitleOptions?
    var autoPlayWhenReady: Bool
    var loopOnEnd: Bool
    var showControls: Bool
    var headers: [String: String]?

    init(
        title: String?,
        subtitle: String?,
        poster: URL?,
        artist: String?,
        rate: Float?,
        subtitles: MediaPlayerSubtitleOptions?,
        autoPlayWhenReady: Bool?,
        loopOnEnd: Bool?,
        showControls: Bool?,
        headers: [String: String]?
    ) {
        self.title = title
        self.subtitle = subtitle
        self.poster = poster
        self.artist = artist
        self.rate = rate ?? 1.0
        self.subtitles = subtitles
        self.autoPlayWhenReady = autoPlayWhenReady ?? false
        self.loopOnEnd = loopOnEnd ?? false
        self.showControls = showControls ?? true
        self.headers = headers
    }
}
