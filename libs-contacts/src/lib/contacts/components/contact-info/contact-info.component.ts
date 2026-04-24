import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { ContactsFacadeService } from '../../services';

@Component({
  selector: 'lib-contact-info',
  templateUrl: './contact-info.component.html',
  styleUrl: './contact-info.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [],
})
export class ContactInfoComponent {
  private readonly facade = inject(ContactsFacadeService);

  readonly title = this.facade.infoTitle;
  readonly contact = this.facade.infoContact;
  readonly errorMessage = this.facade.infoErrorMessage;
  readonly snackbarMessage = this.facade.infoSnackbarMessage;
  readonly isExtraExpanded = this.facade.infoIsExtraExpanded;
  readonly isCallButtonsVisible = this.facade.infoIsCallButtonsVisible;
  readonly isAddToContactsVisible = this.facade.infoIsAddToContactsVisible;
  readonly isDeleteVisible = this.facade.infoIsDeleteVisible;
  readonly extraToggleLabel = this.facade.infoExtraToggleLabel;
  readonly inviteLabel = this.facade.infoInviteLabel;
  readonly deleteLabel = this.facade.infoDeleteLabel;

  back(): void {
    this.facade.infoBack();
  }

  edit(): void {
    this.facade.infoEdit();
  }

  toggleExtra(): void {
    this.facade.infoToggleExtra();
  }

  audioCall(): void {
    this.facade.infoAudioCall();
  }

  videoCall(): void {
    this.facade.infoVideoCall();
  }

  writeMessage(): void {
    this.facade.infoWriteMessage();
  }

  invite(): void {
    this.facade.infoInvite();
  }

  delete(): void {
    this.facade.infoDelete();
  }
}
