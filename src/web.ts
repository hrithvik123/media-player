import { WebPlugin } from '@capacitor/core';

import type { MediaPlayerPlugin } from './definitions';

export class MediaPlayerWeb extends WebPlugin implements MediaPlayerPlugin {
  async echo(options: { value: string }): Promise<{ value: string }> {
    console.log('ECHO', options);
    return options;
  }
}
