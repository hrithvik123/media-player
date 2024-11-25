import type { PluginListenerHandle } from '@capacitor/core';

export interface MediaPlayerPlugin {
  create(options: MediaPlayerOptions): Promise<MediaPlayerResult<string>>;
  play(options: MediaPlayerIdOptions): Promise<MediaPlayerResult<string>>;
  pause(options: MediaPlayerIdOptions): Promise<MediaPlayerResult<string>>;
  getDuration(options: MediaPlayerIdOptions): Promise<MediaPlayerResult<number>>;
  getCurrentTime(options: MediaPlayerIdOptions): Promise<MediaPlayerResult<number>>;
  setCurrentTime(options: MediaPlayerSetCurrentTimeOptions): Promise<MediaPlayerResult<number>>;
  isPlaying(options: MediaPlayerIdOptions): Promise<MediaPlayerResult<boolean>>;
  isMuted(options: MediaPlayerIdOptions): Promise<MediaPlayerResult<boolean>>;
  mute(options: MediaPlayerIdOptions): Promise<MediaPlayerResult<boolean>>;
  getVolume(options: MediaPlayerIdOptions): Promise<MediaPlayerResult<number>>;
  setVolume(options: MediaPlayerSetVolumeOptions): Promise<MediaPlayerResult<number>>;
  getRate(options: MediaPlayerIdOptions): Promise<MediaPlayerResult<number>>;
  setRate(options: MediaPlayerSetRateOptions): Promise<MediaPlayerResult<number>>;
  remove(options: MediaPlayerIdOptions): Promise<MediaPlayerResult<string>>;
  removeAll(): Promise<MediaPlayerResult<string[]>>;

  addListener(
    event: 'MediaPlayer:Ready',
    listener: (event: { playerId: string }) => void,
  ): Promise<PluginListenerHandle>;
  addListener(
    event: 'MediaPlayer:Play',
    listener: (event: { playerId: string }) => void,
  ): Promise<PluginListenerHandle>;
  addListener(
    event: 'MediaPlayer:Pause',
    listener: (event: { playerId: string }) => void,
  ): Promise<PluginListenerHandle>;
  addListener(
    event: 'MediaPlayer:Ended',
    listener: (event: { playerId: string }) => void,
  ): Promise<PluginListenerHandle>;
  addListener(
    event: 'MediaPlayer:Removed',
    listener: (event: { playerId: string }) => void,
  ): Promise<PluginListenerHandle>;
  addListener(
    event: 'MediaPlayer:Seeked',
    listener: (event: { playerId: string; previousTime: number | undefined; newTime: number }) => void,
  ): Promise<PluginListenerHandle>;
  addListener(
    event: 'MediaPlayer:TimeUpdate',
    listener: (event: { playerId: string; currentTime: number }) => void,
  ): Promise<PluginListenerHandle>;
  addListener(
    event: 'MediaPlayer:FullScreen',
    listener: (event: { playerId: string; isInFullScreen: boolean }) => void,
  ): Promise<PluginListenerHandle>;
  addListener(
    event: 'MediaPlayer:PictureInPicture',
    listener: (event: { playerId: string; isInPictureInPicture: boolean }) => void,
  ): Promise<PluginListenerHandle>;

  removeAllListeners(playerId: string): Promise<void>;
}

export type MediaPlayerOptions = {
  playerId: string;
  url: string;
  ios?: MediaPlayerIosOptions;
  android?: MediaPlayerAndroidOptions;
  web?: MediaPlayerWebOptions;
  extra?: MediaPlayerExtraOptions;
};

export type MediaPlayerIdOptions = {
  playerId: string;
};
export type MediaPlayerSetCurrentTimeOptions = {
  playerId: string;
  time: number;
};
export type MediaPlayerSetVolumeOptions = {
  playerId: string;
  volume: number;
};
export type MediaPlayerSetRateOptions = {
  playerId: string;
  rate: number;
};

export type MediaPlayerExtraOptions = {
  title?: string;
  subtitle?: string;
  poster?: string;
  artist?: string;
  rate?: number;
  subtitles?: MediaPlayerSubtitleOptions;
  autoPlayWhenReady?: boolean;
  loopOnEnd?: boolean;
  showControls?: boolean;
  headers?: {
    [key: string]: string;
  };
};

export type MediaPlayerIosOptions = {
  enableExternalPlayback?: boolean;
  enablePiP?: boolean;
  enableBackgroundPlay?: boolean;
  openInFullscreen?: boolean;
  automaticallyEnterPiP?: boolean;
  fullscreenOnLandscape?: boolean;
  top?: number;
  left?: number;
  height?: number;
  width?: number;
};

export type MediaPlayerAndroidOptions = {
  enableChromecast?: boolean;
  enablePiP?: boolean;
  enableBackgroundPlay?: boolean;
  openInFullscreen?: boolean;
  automaticallyEnterPiP?: boolean;
  fullscreenOnLandscape?: boolean;
  top?: number;
  left?: number;
  height?: number;
  width?: number;
};

export type MediaPlayerWebOptions = {
  enableChromecast?: boolean;
};

export type MediaPlayerSubtitleOptions = {
  url: string;
  options?: {
    language?: string;
    foregroundColor?: string;
    backgroundColor?: string;
    fontSize?: number;
  };
};

export type MediaPlayerResult<ResultValueType> = {
  method: string;
  result: boolean;
  value?: ResultValueType;
  error?: Error;
  message?: string;
};
