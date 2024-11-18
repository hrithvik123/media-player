import { WebPlugin } from '@capacitor/core';
import type { MediaPlayerElement } from 'vidstack/elements';
import { VidstackPlayer, VidstackPlayerLayout } from 'vidstack/global/player';

import type {
  MediaPlayerIdOptions,
  MediaPlayerOptions,
  MediaPlayerPlugin,
  MediaPlayerResult,
  MediaPlayerSetCurrentTimeOptions,
  MediaPlayerSetRateOptions,
  MediaPlayerSetVolumeOptions,
} from './definitions';

export class MediaPlayerWeb extends WebPlugin implements MediaPlayerPlugin {
  private readonly _players: Map<string, MediaPlayerElement> = new Map();

  constructor() {
    super();
  }

  async create(options: MediaPlayerOptions): Promise<MediaPlayerResult<string>> {
    try {
      const { playerId, url: src } = options;
      const player = await VidstackPlayer.create({
        target: `#${playerId}`,
        title: options.extra?.title,
        src,
        artist: options.extra?.artist ? options.extra?.artist : undefined,
        poster: options.extra?.poster ? options.extra?.poster : undefined,
        artwork: options.extra?.poster
          ? [
              {
                src: options.extra?.poster,
                sizes: '512x51–2',
                type: 'image/png',
              },
            ]
          : [],
        playsInline: true,
        playbackRate: options.extra?.rate ?? 1,
        controls: true,
        googleCast: {
          androidReceiverCompatible: options.web?.enableChromecast ?? true,
        },
        layout: new VidstackPlayerLayout({
          colorScheme: 'system',
        }),
      });
      this.createPlayerListeners(playerId, player);
      this._players.set(playerId, player);
      return {
        method: 'create',
        result: true,
        value: playerId,
      };
    } catch (err: any) {
      return {
        method: 'create',
        result: false,
        error: err,
        message: err.message,
      };
    }
  }

  private createPlayerListeners(playerId: string, player: MediaPlayerElement): void {
    player.addEventListener('play', () => {
      this.notifyListeners(`MediaPlayer:Play`, {
        playerId,
      });
    });
    player.addEventListener('pause', () => {
      this.notifyListeners(`MediaPlayer:Pause`, {
        playerId,
      });
    });
    player.addEventListener('ended', () => {
      this.notifyListeners(`MediaPlayer:Ended`, {
        playerId,
      });
    });
    player.addEventListener('destroy', () => {
      this.notifyListeners(`MediaPlayer:Removed`, {
        playerId,
      });
    });
    player.addEventListener('seeked', (event) => {
      this.notifyListeners(`MediaPlayer:Seeked`, {
        playerId,
        newTime: event.target.currentTime,
        previousTime: undefined,
      });
    });
    player.addEventListener('time-update', (event) => {
      this.notifyListeners(`MediaPlayer:TimeUpdate`, {
        playerId,
        currentTime: event.target.currentTime,
      });
    });
    player.addEventListener('fullscreen-change', (event) => {
      this.notifyListeners(`MediaPlayer:FullScreen`, {
        playerId,
        isInFullscreen: event.detail,
      });
    });
    player.addEventListener('picture-in-picture-change', (event) => {
      this.notifyListeners(`MediaPlayer:PictureInPicture`, {
        playerId,
        isInPictureInPicture: event.detail,
      });
    });
  }

  private removePlayerListeners(playerId: string, player: MediaPlayerElement): void {
    player.removeEventListener('play', () => {
      this.notifyListeners(`MediaPlayer:Play`, {
        playerId,
      });
    });
    player.removeEventListener('pause', () => {
      this.notifyListeners(`MediaPlayer:Pause`, {
        playerId,
      });
    });
    player.removeEventListener('seeked', () => {
      this.notifyListeners(`MediaPlayer:Seeked`, {
        playerId,
      });
    });
    player.removeEventListener('time-update', (event) => {
      this.notifyListeners(`MediaPlayer:TimeUpdate`, {
        playerId,
        currentTime: event.target.currentTime,
      });
    });
    player.removeEventListener('fullscreen-change', (event) => {
      this.notifyListeners(`MediaPlayer:FullScreen`, {
        playerId,
        isInFullscreen: event.detail,
      });
    });
    player.removeEventListener('picture-in-picture-change', (event) => {
      this.notifyListeners(`MediaPlayer:PictureInPicture`, {
        playerId,
        isInPictureInPicture: event.detail,
      });
    });
  }

  async play(options: MediaPlayerIdOptions): Promise<MediaPlayerResult<string>> {
    const player = this._players.get(options.playerId);
    if (player) {
      await player.play();
      return {
        method: 'play',
        result: true,
        value: options.playerId,
      };
    }
    return {
      method: 'play',
      result: false,
      message: 'Player not found',
    };
  }

  async pause(options: MediaPlayerIdOptions): Promise<MediaPlayerResult<string>> {
    const player = this._players.get(options.playerId);
    if (player) {
      await player.pause();
      return {
        method: 'pause',
        result: true,
        value: options.playerId,
      };
    }
    return {
      method: 'pause',
      result: false,
      message: 'Player not found',
    };
  }

  async getDuration(options: MediaPlayerIdOptions): Promise<MediaPlayerResult<number>> {
    const player = this._players.get(options.playerId);
    if (player) {
      return {
        method: 'getDuration',
        result: true,
        value: player.duration,
      };
    }
    return {
      method: 'getDuration',
      result: false,
      message: 'Player not found',
    };
  }

  async getCurrentTime(options: MediaPlayerIdOptions): Promise<MediaPlayerResult<number>> {
    const player = this._players.get(options.playerId);
    if (player) {
      return {
        method: 'getCurrentTime',
        result: true,
        value: player.currentTime,
      };
    }
    return {
      method: 'getCurrentTime',
      result: false,
      message: 'Player not found',
    };
  }

  async setCurrentTime(options: MediaPlayerSetCurrentTimeOptions): Promise<MediaPlayerResult<number>> {
    const player = this._players.get(options.playerId);
    if (player) {
      player.currentTime = options.time;
      return {
        method: 'setCurrentTime',
        result: true,
        value: options.time,
      };
    }
    return {
      method: 'setCurrentTime',
      result: false,
      message: 'Player not found',
    };
  }

  async isPlaying(options: MediaPlayerIdOptions): Promise<MediaPlayerResult<boolean>> {
    const player = this._players.get(options.playerId);
    if (player) {
      return {
        method: 'isPlaying',
        result: true,
        value: player.$state.playing(),
      };
    }
    return {
      method: 'isPlaying',
      result: false,
      message: 'Player not found',
    };
  }

  async isMuted(options: MediaPlayerIdOptions): Promise<MediaPlayerResult<boolean>> {
    const player = this._players.get(options.playerId);
    if (player) {
      return {
        method: 'isMuted',
        result: true,
        value: player.$state.muted(),
      };
    }
    return {
      method: 'isMuted',
      result: false,
      message: 'Player not found',
    };
  }

  async mute(options: MediaPlayerIdOptions): Promise<MediaPlayerResult<boolean>> {
    const player = this._players.get(options.playerId);
    if (player) {
      player.muted = true;
      return {
        method: 'mute',
        result: true,
        value: true,
      };
    }
    return {
      method: 'mute',
      result: false,
      message: 'Player not found',
    };
  }

  async getVolume(options: MediaPlayerIdOptions): Promise<MediaPlayerResult<number>> {
    const player = this._players.get(options.playerId);
    if (player) {
      return {
        method: 'getVolume',
        result: true,
        value: player.volume,
      };
    }
    return {
      method: 'getVolume',
      result: false,
      message: 'Player not found',
    };
  }

  async setVolume(options: MediaPlayerSetVolumeOptions): Promise<MediaPlayerResult<number>> {
    const player = this._players.get(options.playerId);
    if (player) {
      player.volume = options.volume;
      return {
        method: 'setVolume',
        result: true,
        value: options.volume,
      };
    }
    return {
      method: 'setVolume',
      result: false,
      message: 'Player not found',
    };
  }

  async getRate(options: MediaPlayerIdOptions): Promise<MediaPlayerResult<number>> {
    const player = this._players.get(options.playerId);
    if (player) {
      return {
        method: 'getRate',
        result: true,
        value: player.playbackRate,
      };
    }
    return {
      method: 'getRate',
      result: false,
      message: 'Player not found',
    };
  }

  async setRate(options: MediaPlayerSetRateOptions): Promise<MediaPlayerResult<number>> {
    const player = this._players.get(options.playerId);
    if (player) {
      player.playbackRate = options.rate;
      return {
        method: 'setRate',
        result: true,
        value: options.rate,
      };
    }
    return {
      method: 'setRate',
      result: false,
      message: 'Player not found',
    };
  }

  async remove(options: MediaPlayerIdOptions): Promise<MediaPlayerResult<string>> {
    const player = this._players.get(options.playerId);
    if (player) {
      this.removePlayerListeners(options.playerId, player);
      player.remove();
      this._players.delete(options.playerId);
      return {
        method: 'remove',
        result: true,
        value: options.playerId,
      };
    }
    return {
      method: 'remove',
      result: false,
      message: 'Player not found',
    };
  }

  async removeAll(): Promise<MediaPlayerResult<string[]>> {
    const playersToRemove = Array.from(this._players.keys());
    this._players.forEach((player, playerId) => {
      this.removePlayerListeners(playerId, player);
      player.remove();
    });
    this._players.clear();
    return {
      method: 'removeAll',
      result: true,
      value: playersToRemove,
    };
  }
}
