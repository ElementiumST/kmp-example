import { Component, OnInit, effect, inject, signal } from '@angular/core';
import { Router, RouterOutlet } from '@angular/router';
import { KmpBridgeService } from 'data-access-kmp-bridge';

@Component({
  imports: [RouterOutlet],
  selector: 'app-root',
  templateUrl: './app.html',
  styleUrl: './app.scss',
})
export class App implements OnInit {
  private readonly router = inject(Router);
  private readonly bridge = inject(KmpBridgeService);

  readonly isBootstrapping = signal(true);
  readonly bootstrapError = signal<string | null>(null);

  constructor() {
    effect(() => {
      if (!this.bridge.ready()) {
        return;
      }
      const root = this.bridge.rootChildKind();
      const contactsChild = this.bridge.contactsChildKind();
      const target = this.mapRoute(root, contactsChild);
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

  private mapRoute(
    root: 'AUTH' | 'CONTACTS_LIST' | 'CONTACT_INFO' | 'CONTACT_CREATE' | 'CONTACT_EDIT',
    contactsChild: 'LIST' | 'INFO' | 'CREATE' | 'EDIT',
  ): string {
    if (root === 'AUTH') {
      return 'auth';
    }
    if (root === 'CONTACTS_LIST') {
      if (contactsChild === 'INFO') return 'contacts/info';
      if (contactsChild === 'CREATE') return 'contacts/create';
      if (contactsChild === 'EDIT') return 'contacts/edit';
      return 'contacts';
    }
    if (root === 'CONTACT_INFO') return 'contacts/info';
    if (root === 'CONTACT_CREATE') return 'contacts/create';
    if (root === 'CONTACT_EDIT') return 'contacts/edit';
    return 'auth';
  }
}
