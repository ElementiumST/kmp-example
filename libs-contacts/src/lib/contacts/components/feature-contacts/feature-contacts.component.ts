import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { ContactEditorComponent } from '../contact-editor/contact-editor.component';
import { ContactInfoComponent } from '../contact-info/contact-info.component';
import { ContactsFacadeService } from '../../services';
import { ContactsListComponent } from '../contacts-list/contacts-list.component';

@Component({
  selector: 'lib-feature-contacts',
  templateUrl: './feature-contacts.component.html',
  styleUrl: './feature-contacts.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [
    ContactsListComponent,
    ContactInfoComponent,
    ContactEditorComponent,
  ],
})
export class FeatureContactsComponent {
  private readonly facade = inject(ContactsFacadeService);

  readonly isList = this.facade.isList;
  readonly isInfo = this.facade.isInfo;
  readonly isEditor = this.facade.isEditor;
  readonly info = this.facade.info;
  readonly editor = this.facade.editor;
}
