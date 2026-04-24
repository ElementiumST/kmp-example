import { Component, OnDestroy, OnInit, effect, inject, signal } from '@angular/core';
import { Router, RouterOutlet } from '@angular/router';
import { KmpBridgeService } from 'data-access-kmp-bridge';

@Component({
  imports: [RouterOutlet],
  selector: 'app-root',
  templateUrl: './app.html',
  styleUrl: './app.scss',
})
export class App implements OnInit, OnDestroy {
  private readonly router = inject(Router);
  private readonly bridge = inject(KmpBridgeService);

  readonly isBootstrapping = signal(true);
  readonly bootstrapError = signal<string | null>(null);

  constructor() {
    effect(() => {
      if (!this.bridge.ready()) {
        return;
      }
      const target = this.bridge.routePath();
      if (this.router.url !== `/${target}`) {
        void this.router.navigateByUrl(target, { replaceUrl: true });
      }
    });
  }

  async ngOnInit(): Promise<void> {
    try {
      await this.bridge.initialize('/api/rest');
      this.isBootstrapping.set(false);
    } catch (error) {
      const message = error instanceof Error ? error.message : 'Unknown bootstrap error';
      this.bootstrapError.set(message);
      this.isBootstrapping.set(false);
    }
  }

  ngOnDestroy(): void {
    this.bridge.destroy();
  }
}
