import { CommonModule } from '@angular/common';
import { Component, computed, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ContactItem, KmpBridgeService } from 'data-access-kmp-bridge';

@Component({
  selector: 'lib-feature-contacts',
  imports: [CommonModule, FormsModule],
  templateUrl: './feature-contacts.html',
  styleUrl: './feature-contacts.css',
})
export class FeatureContacts {
  private readonly bridge = inject(KmpBridgeService);
  readonly listState = this.bridge.contactsListState;
  readonly childKind = this.bridge.contactsChildKind;
  readonly infoState = this.bridge.contactInfoState;
  readonly editorState = this.bridge.contactEditorState;

  readonly isList = computed(() => this.childKind() === 'LIST');
  readonly isInfo = computed(() => this.childKind() === 'INFO');
  readonly isEditor = computed(() => this.childKind() === 'CREATE' || this.childKind() === 'EDIT');

  readonly canLoadMore = computed(() => this.listState().hasMore && !this.listState().isLoadingMore);

  trackContact(_index: number, item: ContactItem): string {
    return item.contactId || item.profileId || item.name;
  }

  updateSearch(value: string): void {
    this.bridge.contactsUpdateQuery(value);
  }

  refresh(): void {
    this.bridge.contactsRefresh();
  }

  openAddOverlay(): void {
    this.bridge.contactsOpenAddOverlay();
  }

  closeAddOverlay(): void {
    this.bridge.contactsCloseAddOverlay();
  }

  openCreate(): void {
    this.bridge.contactsOpenCreate();
  }

  openInfo(index: number): void {
    this.bridge.contactsOpenInfo(index);
  }

  openEdit(index: number): void {
    this.bridge.contactsOpenEdit(index);
  }

  deleteFromMenu(index: number): void {
    this.bridge.contactsDeleteFromMenu(index);
  }

  loadMore(): void {
    this.bridge.contactsLoadMore();
  }

  updateAddOverlayQuery(value: string): void {
    this.bridge.contactsUpdateAddOverlayQuery(value);
  }

  loadMoreOverlay(): void {
    this.bridge.contactsLoadMoreAddOverlay();
  }

  inviteInterlocutor(profileId: string): void {
    this.bridge.contactsInviteInterlocutor(profileId);
  }

  infoBack(): void {
    this.bridge.contactInfoBack();
  }

  infoEdit(): void {
    this.bridge.contactInfoEdit();
  }

  infoDelete(): void {
    this.bridge.contactInfoDelete();
  }

  infoInvite(): void {
    this.bridge.contactInfoInvite();
  }

  infoToggleExtra(): void {
    this.bridge.contactInfoToggleExtra();
  }

  infoAudioCall(): void {
    this.bridge.contactInfoAudioCall();
  }

  infoVideoCall(): void {
    this.bridge.contactInfoVideoCall();
  }

  infoWriteMessage(): void {
    this.bridge.contactInfoWriteMessage();
  }

  editorBack(): void {
    this.bridge.contactEditorBack();
  }

  editorSave(): void {
    this.bridge.contactEditorSave();
  }

  editorConfirmLeave(): void {
    this.bridge.contactEditorConfirmLeave();
  }

  editorCancelLeave(): void {
    this.bridge.contactEditorCancelLeave();
  }

  updateEditorName(value: string): void {
    this.bridge.contactEditorUpdateName(value);
  }

  updateEditorEmail(value: string): void {
    this.bridge.contactEditorUpdateEmail(value);
  }

  updateEditorPhone(value: string): void {
    this.bridge.contactEditorUpdatePhone(value);
  }

  updateEditorNote(value: string): void {
    this.bridge.contactEditorUpdateNote(value);
  }

  updateEditorTags(value: string): void {
    this.bridge.contactEditorUpdateTags(value);
  }

  errorMessage(code: string | null): string {
    if (!code) return '';
    if (code === 'EMPTY') return 'Поле обязательно';
    if (code === 'TOO_LONG') return 'Превышена максимальная длина';
    if (code === 'INVALID_FORMAT') return 'Неверный формат';
    return code;
  }
}
