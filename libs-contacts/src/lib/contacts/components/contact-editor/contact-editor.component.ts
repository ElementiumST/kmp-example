import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { ContactsFacadeService } from '../../services';

@Component({
  selector: 'lib-contact-editor',
  templateUrl: './contact-editor.component.html',
  styleUrl: './contact-editor.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [],
})
export class ContactEditorComponent {
  private readonly facade = inject(ContactsFacadeService);

  readonly title = this.facade.editorTitle;
  readonly isNote = this.facade.editorIsNote;
  readonly errorMessage = this.facade.editorErrorMessage;
  readonly draft = this.facade.editorDraft;
  readonly nameError = this.facade.editorNameError;
  readonly emailError = this.facade.editorEmailError;
  readonly phoneError = this.facade.editorPhoneError;
  readonly noteError = this.facade.editorNoteError;
  readonly tagsError = this.facade.editorTagsError;
  readonly saveDisabled = this.facade.editorSaveDisabled;
  readonly saveLabel = this.facade.editorSaveLabel;
  readonly showLeaveConfirmation = this.facade.editorShowLeaveConfirmation;

  back(): void {
    this.facade.editorBack();
  }

  save(): void {
    this.facade.editorSave();
  }

  confirmLeave(): void {
    this.facade.editorConfirmLeave();
  }

  cancelLeave(): void {
    this.facade.editorCancelLeave();
  }

  onNameInput(value: string): void {
    this.facade.updateEditorName(value);
  }

  onEmailInput(value: string): void {
    this.facade.updateEditorEmail(value);
  }

  onPhoneInput(value: string): void {
    this.facade.updateEditorPhone(value);
  }

  onNoteInput(value: string): void {
    this.facade.updateEditorNote(value);
  }

  onTagsInput(value: string): void {
    this.facade.updateEditorTags(value);
  }
}
