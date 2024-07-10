import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

import SharedModule from 'app/shared/shared.module';
import { ITEM_DELETED_EVENT } from 'app/config/navigation.constants';
import { ISessionPackage } from '../session-package.model';
import { SessionPackageService } from '../service/session-package.service';

@Component({
  standalone: true,
  templateUrl: './session-package-delete-dialog.component.html',
  imports: [SharedModule, FormsModule],
})
export class SessionPackageDeleteDialogComponent {
  sessionPackage?: ISessionPackage;

  constructor(
    protected sessionPackageService: SessionPackageService,
    protected activeModal: NgbActiveModal,
  ) {}

  cancel(): void {
    this.activeModal.dismiss();
  }

  confirmDelete(id: string): void {
    this.sessionPackageService.delete(id).subscribe(() => {
      this.activeModal.close(ITEM_DELETED_EVENT);
    });
  }
}
