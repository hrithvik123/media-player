import { registerPlugin } from '@capacitor/core';

import type { MediaPlayerPlugin } from './definitions';

const MediaPlayer = registerPlugin<MediaPlayerPlugin>('MediaPlayer', {
  web: () => import('./web').then((m) => new m.MediaPlayerWeb()),
});

export { MediaPlayerOptions } from './definitions';
export { MediaPlayer };
