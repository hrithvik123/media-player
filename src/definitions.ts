export interface MediaPlayerPlugin {
  echo(options: { value: string }): Promise<{ value: string }>;
}
