import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { ContactsFacadeService } from '../../services';

@Component({
  selector: 'lib-contact-add-overlay',
  templateUrl: './contact-add-overlay.component.html',
  styleUrl: './contact-add-overlay.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [],
})
export class ContactAddOverlayComponent {
  private readonly facade = inject(ContactsFacadeService);

  readonly query = this.facade.addOverlayQuery;
  readonly errorMessage = this.facade.addOverlayErrorMessage;
  readonly items = this.facade.addOverlayItems;
  readonly canLoadMore = this.facade.addOverlayCanLoadMore;
  readonly trackInterlocutor = this.facade.trackInterlocutor;

  inviteLabel = this.facade.inviteLabel.bind(this.facade);

  onQueryInput(value: string): void {
    this.facade.updateAddOverlayQuery(value);
  }

  createNote(): void {
    this.facade.openCreate();
    this.facade.closeAddOverlay();
  }

  close(): void {
    this.facade.closeAddOverlay();
  }

  invite(profileId: string): void {
    this.facade.inviteInterlocutor(profileId);
  }

  loadMore(): void {
    this.facade.loadMoreAddOverlay();
  }
}
