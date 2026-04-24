import {
  type ContactAddOverlayState,
  type ContactItem,
  type ContactEditorState,
} from 'data-access-kmp-bridge';

export const EMPTY_ADD_OVERLAY: ContactAddOverlayState = {
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
};

export const EMPTY_CONTACT: ContactItem = {
  contactId: '',
  profileId: '',
  name: '',
  email: '',
  phone: '',
  note: '',
  tags: [],
  interlocutorType: '',
  avatarUrl: '',
  aboutSelf: '',
  additionalContact: '',
  externalDomainHost: '',
  externalDomainName: '',
  presence: '',
  isNote: false,
  isInContacts: false,
};

export const EMPTY_EDITOR_DRAFT: ContactEditorState['draft'] = {
  name: '',
  email: '',
  phone: '',
  note: '',
  tagsText: '',
};

export const EMPTY_EDITOR_VALIDATION: ContactEditorState['validation'] = {
  isValid: false,
  name: null,
  email: null,
  phone: null,
  note: null,
  tags: null,
};
