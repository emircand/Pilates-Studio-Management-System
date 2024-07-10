import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

import SharedModule from 'app/shared/shared.module';
import { ITEM_DELETED_EVENT } from 'app/config/navigation.constants';
import { IQRCode } from '../qr-code.model';
import { QRCodeService } from '../service/qr-code.service';

@Component({
  standalone: true,
  templateUrl: './qr-code-delete-dialog.component.html',
  imports: [SharedModule, FormsModule],
})
export class QRCodeDeleteDialogComponent {
  qRCode?: IQRCode;

  constructor(
    protected qRCodeService: QRCodeService,
    protected activeModal: NgbActiveModal,
  ) {}

  cancel(): void {
    this.activeModal.dismiss();
  }

  confirmDelete(id: number): void {
    this.qRCodeService.delete(id).subscribe(() => {
      this.activeModal.close(ITEM_DELETED_EVENT);
    });
  }
}
