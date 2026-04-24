import { Injectable, signal } from '@angular/core';
import {
  generatedRouteTable,
  type AuthState,
  type ContactItem,
  type ContactEditorState,
  type ContactInfoState,
  type ContactsChildKind,
  type ContactsListState,
  type InterlocutorItem,
  type RootChildKind,
} from './generated/bridge-types';

type StateSubscription = { cancel: () => void };

type KmpBridge = {
  currentRootChildKind: () => RootChildKind;
  watchRootChildKind: (observer: (value: RootChildKind) => void) => StateSubscription;

  authStateJson: () => string;
  watchAuthState: (observer: (value: string) => void) => StateSubscription;
  authUpdateLogin: (value: string) => void;
  authUpdatePassword: (value: string) => void;
  authSubmit: () => void;

  contactsChildKind: () => ContactsChildKind;
  watchContactsChildKind: (observer: (value: ContactsChildKind) => void) => StateSubscription;
  contactsListStateJson: () => string;
  watchContactsListState: (observer: (value: string) => void) => StateSubscription;
  contactsRefresh: () => void;
  contactsLoadMore: () => void;
  contactsUpdateQuery: (value: string) => void;
  contactsOpenInfo: (index: number) => void;
  contactsOpenAddOverlay: () => void;
  contactsCloseAddOverlay: () => void;
  contactsOpenCreate: () => void;
  contactsOpenEdit: (index: number) => void;
  contactsDeleteFromMenu: (index: number) => void;
  contactsUpdateAddOverlayQuery: (value: string) => void;
  contactsLoadMoreAddOverlay: () => void;
  contactsInviteInterlocutor: (profileId: string) => void;

  contactInfoStateJson: () => string;
  watchContactInfoState: (observer: (value: string) => void) => StateSubscription;
  contactInfoBack: () => void;
  contactInfoEdit: () => void;
  contactInfoDelete: () => void;
  contactInfoInvite: () => void;
  contactInfoToggleExtra: () => void;
  contactInfoWriteMessage: () => void;
  contactInfoAudioCall: () => void;
  contactInfoVideoCall: () => void;

  contactEditorStateJson: () => string;
  watchContactEditorState: (observer: (value: string) => void) => StateSubscription;
  contactEditorUpdateName: (value: string) => void;
  contactEditorUpdateEmail: (value: string) => void;
  contactEditorUpdatePhone: (value: string) => void;
  contactEditorUpdateNote: (value: string) => void;
  contactEditorUpdateTags: (value: string) => void;
  contactEditorSave: () => void;
  contactEditorBack: () => void;
  contactEditorConfirmLeave: () => void;
  contactEditorCancelLeave: () => void;
  destroy: () => void;
};

type KmpCoreGlobal = {
  WebBridgeFactory?: {
    create: (baseUrl: string) => KmpBridge;
  };
  com?: {
    example?: {
      kmpexample?: {
        kmp?: {
          core?: {
            bridge?: {
              web?: {
                WebBridgeFactory?: {
                  create: (baseUrl: string) => KmpBridge;
                };
              };
            };
          };
        };
      };
    };
  };
};

function parseJson<T>(value: string, fallback: T): T {
  try {
    return JSON.parse(value) as T;
  } catch {
    return fallback;
  }
}

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === 'object' && value !== null && !Array.isArray(value);
}

function normalizeContactsListState(
  raw: unknown,
  fallback: ContactsListState,
): ContactsListState {
  if (!isRecord(raw)) {
    return fallback;
  }

  const root = raw as Record<string, unknown>;
  const addOverlayRaw = isRecord(root['addOverlay']) ? (root['addOverlay'] as Record<string, unknown>) : {};

  return {
    ...fallback,
    ...root,
    items: Array.isArray(root['items']) ? (root['items'] as ContactItem[]) : fallback.items,
    presence: isRecord(root['presence']) ? (root['presence'] as Record<string, string>) : fallback.presence,
    addOverlay: {
      ...fallback.addOverlay,
      ...addOverlayRaw,
      items: Array.isArray(addOverlayRaw['items']) ? (addOverlayRaw['items'] as InterlocutorItem[]) : fallback.addOverlay.items,
      invitingProfileIds: Array.isArray(addOverlayRaw['invitingProfileIds'])
        ? (addOverlayRaw['invitingProfileIds'] as string[])
        : fallback.addOverlay.invitingProfileIds,
    },
  };
}

function resolveWebBridgeFactory(core: KmpCoreGlobal | undefined) {
  return core?.WebBridgeFactory ?? core?.com?.example?.kmpexample?.kmp?.core?.bridge?.web?.WebBridgeFactory;
}

async function loadSharedCoreScripts(): Promise<void> {
  const manifestResponse = await fetch('/shared-core/manifest.json');
  if (!manifestResponse.ok) {
    throw new Error('Failed to load KMP shared-core manifest.');
  }

  const manifest = (await manifestResponse.json()) as { files: string[] };
  for (const file of manifest.files) {
    await new Promise<void>((resolve, reject) => {
      const script = document.createElement('script');
      script.src = `/shared-core/${file}`;
      script.async = false;
      script.onload = () => resolve();
      script.onerror = () => reject(new Error(`Failed to load script: ${file}`));
      document.head.appendChild(script);
    });
  }
}

@Injectable({ providedIn: 'root' })
export class KmpBridgeService {
  private readonly rootChildDefault: RootChildKind = 'AUTH';
  private readonly contactsChildDefault: ContactsChildKind = 'LIST';

  readonly rootChildKind = signal<RootChildKind>(this.rootChildDefault);
  readonly contactsChildKind = signal<ContactsChildKind>(this.contactsChildDefault);
  readonly authState = signal<AuthState>({
    login: '',
    password: '',
    isLoading: false,
    isAuthorized: false,
    errorMessage: null,
    sessionId: null,
    authorizedLogin: null,
    authorizedName: null,
    submitLabel: 'Войти',
    canSubmit: false,
  });
  readonly contactsListState = signal<ContactsListState>({
    query: '',
    total: 0,
    isLoading: false,
    isLoadingMore: false,
    isRefreshing: false,
    errorMessage: null,
    hasMore: false,
    isAddOverlayVisible: false,
    contextMenuContactIndex: -1,
    snackbarMessage: null,
    items: [],
    presence: {},
    addOverlay: {
      query: '',
      isLoading: false,
      isLoadingMore: false,
      errorMessage: null,
      hasMore: false,
      foreignOffset: 0,
      companyOffset: 0,
      directoryOffset: 0,
      domainOffset: 0,
      items: [],
      invitingProfileIds: [],
    },
  });
  readonly contactInfoState = signal<ContactInfoState | null>(null);
  readonly contactEditorState = signal<ContactEditorState | null>(null);
  readonly routePath = signal('auth');
  readonly ready = signal(false);

  private bridge: KmpBridge | null = null;
  private initPromise: Promise<void> | null = null;
  private subscriptions: StateSubscription[] = [];
  private contactsChildSubscription: StateSubscription | null = null;
  private contactsListSubscription: StateSubscription | null = null;
  private infoSubscription: StateSubscription | null = null;
  private editorSubscription: StateSubscription | null = null;

  initialize(baseUrl = '/api/rest'): Promise<void> {
    if (this.ready()) {
      return Promise.resolve();
    }
    if (this.initPromise) {
      return this.initPromise;
    }

    this.initPromise = (async () => {
      await loadSharedCoreScripts();
      const globals = globalThis as unknown as {
        ['kmp-example.kmp:bridge-web']?: KmpCoreGlobal;
        ['kmp-example.kmp:core']?: KmpCoreGlobal;
      };
      const core = globals['kmp-example.kmp:bridge-web'] ?? globals['kmp-example.kmp:core'];
      const webBridgeFactory = resolveWebBridgeFactory(core);
      if (!webBridgeFactory) {
        throw new Error('KMP WebBridgeFactory is missing from shared-core bundle.');
      }

      this.bridge = webBridgeFactory.create(baseUrl);
      this.bindRootSubscriptions();
      this.ready.set(true);
    })();

    return this.initPromise;
  }

  private bindRootSubscriptions(): void {
    const bridge = this.requireBridge();
    this.cancelAllSubscriptions();

    this.rootChildKind.set(bridge.currentRootChildKind());
    this.authState.set(parseJson(bridge.authStateJson(), this.authState()));
    this.updateRoutePath();
    this.syncNestedStates();

    this.subscriptions.push(
      bridge.watchRootChildKind((value) => {
        this.rootChildKind.set(value);
        this.updateRoutePath();
        this.syncNestedStates();
      }),
    );
    this.subscriptions.push(
      bridge.watchAuthState((value) => {
        this.authState.set(parseJson(value, this.authState()));
      }),
    );
  }

  private syncNestedStates(): void {
    const bridge = this.requireBridge();

    this.contactsChildSubscription?.cancel();
    this.contactsListSubscription?.cancel();
    this.contactsChildSubscription = null;
    this.contactsListSubscription = null;

    this.contactsChildKind.set(bridge.contactsChildKind());
    this.contactsListState.set(
      normalizeContactsListState(parseJson<unknown>(bridge.contactsListStateJson(), null), this.contactsListState()),
    );

    this.contactsChildSubscription = bridge.watchContactsChildKind((value) => {
      this.contactsChildKind.set(value);
      this.updateRoutePath();
      this.syncDetailSubscriptions();
    });
    this.contactsListSubscription = bridge.watchContactsListState((value) => {
      this.contactsListState.set(
        normalizeContactsListState(parseJson<unknown>(value, null), this.contactsListState()),
      );
    });
    this.syncDetailSubscriptions();
    this.updateRoutePath();
  }

  private syncDetailSubscriptions(): void {
    const bridge = this.requireBridge();
    this.infoSubscription?.cancel();
    this.editorSubscription?.cancel();
    this.infoSubscription = null;
    this.editorSubscription = null;

    const infoNow = parseJson<ContactInfoState | null>(bridge.contactInfoStateJson(), null);
    const editorNow = parseJson<ContactEditorState | null>(bridge.contactEditorStateJson(), null);
    this.contactInfoState.set(infoNow && infoNow.contact ? infoNow : null);
    this.contactEditorState.set(editorNow && editorNow.draft ? editorNow : null);

    this.infoSubscription = bridge.watchContactInfoState((value) => {
      const next = parseJson<ContactInfoState | null>(value, null);
      this.contactInfoState.set(next && next.contact ? next : null);
    });
    this.editorSubscription = bridge.watchContactEditorState((value) => {
      const next = parseJson<ContactEditorState | null>(value, null);
      this.contactEditorState.set(next && next.draft ? next : null);
    });
  }

  authUpdateLogin(value: string): void {
    this.requireBridge().authUpdateLogin(value);
  }

  authUpdatePassword(value: string): void {
    this.requireBridge().authUpdatePassword(value);
  }

  authSubmit(): void {
    this.requireBridge().authSubmit();
  }

  contactsRefresh(): void {
    this.requireBridge().contactsRefresh();
  }

  contactsLoadMore(): void {
    this.requireBridge().contactsLoadMore();
  }

  contactsUpdateQuery(value: string): void {
    this.requireBridge().contactsUpdateQuery(value);
  }

  contactsOpenInfo(index: number): void {
    this.requireBridge().contactsOpenInfo(index);
  }

  contactsOpenAddOverlay(): void {
    this.requireBridge().contactsOpenAddOverlay();
  }

  contactsCloseAddOverlay(): void {
    this.requireBridge().contactsCloseAddOverlay();
  }

  contactsOpenCreate(): void {
    this.requireBridge().contactsOpenCreate();
  }

  contactsOpenEdit(index: number): void {
    this.requireBridge().contactsOpenEdit(index);
  }

  contactsDeleteFromMenu(index: number): void {
    this.requireBridge().contactsDeleteFromMenu(index);
  }

  contactsUpdateAddOverlayQuery(value: string): void {
    this.requireBridge().contactsUpdateAddOverlayQuery(value);
  }

  contactsLoadMoreAddOverlay(): void {
    this.requireBridge().contactsLoadMoreAddOverlay();
  }

  contactsInviteInterlocutor(profileId: string): void {
    this.requireBridge().contactsInviteInterlocutor(profileId);
  }

  contactInfoBack(): void {
    this.requireBridge().contactInfoBack();
  }

  contactInfoEdit(): void {
    this.requireBridge().contactInfoEdit();
  }

  contactInfoDelete(): void {
    this.requireBridge().contactInfoDelete();
  }

  contactInfoInvite(): void {
    this.requireBridge().contactInfoInvite();
  }

  contactInfoToggleExtra(): void {
    this.requireBridge().contactInfoToggleExtra();
  }

  contactInfoWriteMessage(): void {
    this.requireBridge().contactInfoWriteMessage();
  }

  contactInfoAudioCall(): void {
    this.requireBridge().contactInfoAudioCall();
  }

  contactInfoVideoCall(): void {
    this.requireBridge().contactInfoVideoCall();
  }

  contactEditorUpdateName(value: string): void {
    this.requireBridge().contactEditorUpdateName(value);
  }

  contactEditorUpdateEmail(value: string): void {
    this.requireBridge().contactEditorUpdateEmail(value);
  }

  contactEditorUpdatePhone(value: string): void {
    this.requireBridge().contactEditorUpdatePhone(value);
  }

  contactEditorUpdateNote(value: string): void {
    this.requireBridge().contactEditorUpdateNote(value);
  }

  contactEditorUpdateTags(value: string): void {
    this.requireBridge().contactEditorUpdateTags(value);
  }

  contactEditorSave(): void {
    this.requireBridge().contactEditorSave();
  }

  contactEditorBack(): void {
    this.requireBridge().contactEditorBack();
  }

  contactEditorConfirmLeave(): void {
    this.requireBridge().contactEditorConfirmLeave();
  }

  contactEditorCancelLeave(): void {
    this.requireBridge().contactEditorCancelLeave();
  }

  private cancelAllSubscriptions(): void {
    for (const subscription of this.subscriptions) {
      subscription.cancel();
    }
    this.subscriptions = [];
    this.contactsChildSubscription?.cancel();
    this.contactsListSubscription?.cancel();
    this.infoSubscription?.cancel();
    this.editorSubscription?.cancel();
    this.contactsChildSubscription = null;
    this.contactsListSubscription = null;
    this.infoSubscription = null;
    this.editorSubscription = null;
  }

  destroy(): void {
    this.cancelAllSubscriptions();
    this.bridge?.destroy();
    this.bridge = null;
    this.ready.set(false);
  }

  private updateRoutePath(): void {
    const root = this.rootChildKind();
    const contacts = this.contactsChildKind();
    const matched = generatedRouteTable.find((item) => item.root === root && item.contacts === contacts);
    this.routePath.set(matched?.path ?? 'auth');
  }

  private requireBridge(): KmpBridge {
    if (!this.bridge) {
      throw new Error('KMP bridge is not initialized.');
    }
    return this.bridge;
  }
}
