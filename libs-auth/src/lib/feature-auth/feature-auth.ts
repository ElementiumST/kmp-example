import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { KmpBridgeService } from 'data-access-kmp-bridge';

@Component({
  selector: 'lib-feature-auth',
  imports: [CommonModule, FormsModule],
  templateUrl: './feature-auth.html',
  styleUrl: './feature-auth.css',
})
export class FeatureAuth {
  private readonly bridge = inject(KmpBridgeService);
  readonly state = this.bridge.authState;

  onLoginChange(value: string): void {
    this.bridge.authUpdateLogin(value);
  }

  onPasswordChange(value: string): void {
    this.bridge.authUpdatePassword(value);
  }

  submit(): void {
    this.bridge.authSubmit();
  }
}
