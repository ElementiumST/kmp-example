import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { ContactAddOverlayComponent } from '../contact-add-overlay/contact-add-overlay.component';
import { ContactsFacadeService } from '../../services';

@Component({
  selector: 'lib-contacts-list',
  templateUrl: './contacts-list.component.html',
  styleUrl: './contacts-list.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [ContactAddOverlayComponent],
})
export class ContactsListComponent {
  private readonly facade = inject(ContactsFacadeService);

  readonly query = this.facade.listQuery;
  readonly items = this.facade.listItems;
  readonly hasItems = this.facade.listHasItems;
  readonly isInitialLoading = this.facade.listIsInitialLoading;
  readonly errorMessage = this.facade.listErrorMessage;
  readonly snackbarMessage = this.facade.listSnackbarMessage;
  readonly canLoadMore = this.facade.listCanLoadMore;
  readonly isAddOverlayVisible = this.facade.isAddOverlayVisible;
  readonly trackContact = this.facade.trackContact;

  onQueryInput(value: string): void {
    this.facade.updateSearch(value);
  }

  refresh(): void {
    this.facade.refresh();
  }

  openAddOverlay(): void {
    this.facade.openAddOverlay();
  }

  openInfo(index: number): void {
    this.facade.openInfo(index);
  }

  openEdit(index: number): void {
    this.facade.openEdit(index);
  }

  deleteFromMenu(index: number): void {
    this.facade.deleteFromMenu(index);
  }

  loadMore(): void {
    this.facade.loadMore();
  }
}
