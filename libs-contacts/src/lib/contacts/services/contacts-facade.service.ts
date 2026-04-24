import { Injectable, Signal, computed, inject } from '@angular/core';
import {
  type ContactAddOverlayState,
  type ContactEditorState,
  type ContactInfoState,
  type ContactItem,
  type ContactsChildKind,
  type ContactsListState,
  type InterlocutorItem,
  KmpBridgeService,
} from 'data-access-kmp-bridge';
import {
  EMPTY_ADD_OVERLAY,
  EMPTY_CONTACT,
  EMPTY_EDITOR_DRAFT,
  EMPTY_EDITOR_VALIDATION,
} from '../model';
import { mapEditorErrorCode } from '../utils';

@Injectable({ providedIn: 'root' })
export class ContactsFacadeService {
  private readonly bridge = inject(KmpBridgeService);

  private readonly childKind: Signal<ContactsChildKind> = this.bridge.contactsChildKind;
  private readonly listState: Signal<ContactsListState> = this.bridge.contactsListState;
  private readonly infoState: Signal<ContactInfoState | null> = this.bridge.contactInfoState;
  private readonly editorState: Signal<ContactEditorState | null> = this.bridge.contactEditorState;

  readonly isList = computed<boolean>(() => this.childKind() === 'LIST');
  readonly isInfo = computed<boolean>(() => this.childKind() === 'INFO');
  readonly isEditor = computed<boolean>(() => {
    const kind = this.childKind();

    return kind === 'CREATE' || kind === 'EDIT';
  });

  readonly listQuery = computed<string>(() => this.listState().query);
  readonly listItems = computed<readonly ContactItem[]>(() => this.listState().items);
  readonly listHasItems = computed<boolean>(() => this.listItems().length > 0);
  readonly listIsInitialLoading = computed<boolean>(
    () => this.listState().isLoading && !this.listHasItems(),
  );
  readonly listErrorMessage = computed<string | null>(() => this.listState().errorMessage);
  readonly listSnackbarMessage = computed<string | null>(() => this.listState().snackbarMessage);
  readonly listCanLoadMore = computed<boolean>(() => {
    const state = this.listState();

    return state.hasMore && !state.isLoadingMore;
  });

  readonly isAddOverlayVisible = computed<boolean>(() => this.listState().isAddOverlayVisible);

  private readonly addOverlay = computed<ContactAddOverlayState>(
    () => this.listState().addOverlay ?? EMPTY_ADD_OVERLAY,
  );
  readonly addOverlayQuery = computed<string>(() => this.addOverlay().query);
  readonly addOverlayErrorMessage = computed<string | null>(() => this.addOverlay().errorMessage);
  readonly addOverlayItems = computed<readonly InterlocutorItem[]>(() => this.addOverlay().items);
  readonly addOverlayCanLoadMore = computed<boolean>(() => {
    const overlay = this.addOverlay();

    return overlay.hasMore && !overlay.isLoadingMore;
  });

  private readonly addOverlayInvitingIds = computed<readonly string[]>(
    () => this.addOverlay().invitingProfileIds,
  );

  readonly info: Signal<ContactInfoState | null> = this.infoState;
  readonly infoContact = computed<ContactItem>(() => this.info()?.contact ?? EMPTY_CONTACT);
  readonly infoTitle = computed<string>(() => this.infoContact().name || 'Контакт');
  readonly infoErrorMessage = computed<string | null>(() => this.info()?.errorMessage ?? null);
  readonly infoSnackbarMessage = computed<string | null>(() => this.info()?.snackbarMessage ?? null);
  readonly infoIsExtraExpanded = computed<boolean>(() => this.info()?.isExtraExpanded ?? false);
  readonly infoIsCallButtonsVisible = computed<boolean>(
    () => this.info()?.isCallButtonsVisible ?? false,
  );
  readonly infoIsAddToContactsVisible = computed<boolean>(
    () => this.info()?.isAddToContactsVisible ?? false,
  );
  readonly infoIsDeleteVisible = computed<boolean>(() => this.info()?.isDeleteVisible ?? false);
  readonly infoIsInviting = computed<boolean>(() => this.info()?.isInviting ?? false);
  readonly infoIsDeleting = computed<boolean>(() => this.info()?.isDeleting ?? false);
  readonly infoExtraToggleLabel = computed<string>(
    () => this.infoIsExtraExpanded() ? 'Скрыть подробности' : 'Показать подробности',
  );
  readonly infoInviteLabel = computed<string>(
    () => this.infoIsInviting() ? 'Отправка...' : 'Добавить в контакты',
  );
  readonly infoDeleteLabel = computed<string>(
    () => this.infoIsDeleting() ? 'Удаление...' : 'Удалить контакт',
  );

  readonly editor: Signal<ContactEditorState | null> = this.editorState;
  readonly editorMode = computed<'CREATE' | 'EDIT'>(() => this.editor()?.mode ?? 'CREATE');
  readonly editorTitle = computed<string>(
    () => this.editorMode() === 'CREATE' ? 'Новая заметка' : 'Редактирование',
  );
  readonly editorIsNote = computed<boolean>(() => this.editor()?.isNote ?? false);
  readonly editorErrorMessage = computed<string | null>(() => this.editor()?.errorMessage ?? null);
  readonly editorDraft = computed<ContactEditorState['draft']>(
    () => this.editor()?.draft ?? EMPTY_EDITOR_DRAFT,
  );

  private readonly editorValidation = computed<ContactEditorState['validation']>(
    () => this.editor()?.validation ?? EMPTY_EDITOR_VALIDATION,
  );
  readonly editorNameError = computed<string>(() => mapEditorErrorCode(this.editorValidation().name));
  readonly editorEmailError = computed<string>(() => mapEditorErrorCode(this.editorValidation().email));
  readonly editorPhoneError = computed<string>(() => mapEditorErrorCode(this.editorValidation().phone));
  readonly editorNoteError = computed<string>(() => mapEditorErrorCode(this.editorValidation().note));
  readonly editorTagsError = computed<string>(() => mapEditorErrorCode(this.editorValidation().tags));

  readonly editorCanSave = computed<boolean>(() => this.editor()?.canSave ?? false);
  readonly editorIsSaving = computed<boolean>(() => this.editor()?.isSaving ?? false);
  readonly editorShowLeaveConfirmation = computed<boolean>(
    () => this.editor()?.showLeaveConfirmation ?? false,
  );
  readonly editorSaveDisabled = computed<boolean>(
    () => !this.editorCanSave() || this.editorIsSaving(),
  );
  readonly editorSaveLabel = computed<string>(() => {
    if (this.editorIsSaving()) {
      return 'Сохранение...';
    }

    return this.editorMode() === 'CREATE' ? 'Создать' : 'Сохранить';
  });

  trackContact = (_index: number, item: ContactItem): string => {
    return item.contactId || item.profileId || item.name;
  };

  trackInterlocutor = (_index: number, item: InterlocutorItem): string => {
    return item.profileId;
  };

  isInviting(profileId: string): boolean {
    return this.addOverlayInvitingIds().includes(profileId);
  }

  inviteLabel(item: Pick<InterlocutorItem, 'profileId' | 'isInContacts'>): string {
    if (this.isInviting(item.profileId)) {
      return 'Отправка...';
    }

    return item.isInContacts ? 'Уже в контактах' : 'Добавить';
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

  loadMoreAddOverlay(): void {
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
}
