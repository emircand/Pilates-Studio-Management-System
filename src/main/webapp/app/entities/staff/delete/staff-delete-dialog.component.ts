import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

import SharedModule from 'app/shared/shared.module';
import { ITEM_DELETED_EVENT } from 'app/config/navigation.constants';
import { IStaff } from '../staff.model';
import { StaffService } from '../service/staff.service';

@Component({
  standalone: true,
  templateUrl: './staff-delete-dialog.component.html',
  imports: [SharedModule, FormsModule],
})
export class StaffDeleteDialogComponent {
  staff?: IStaff;

  constructor(
    protected staffService: StaffService,
    protected activeModal: NgbActiveModal,
  ) {}

  cancel(): void {
    this.activeModal.dismiss();
  }

  confirmDelete(id: number): void {
    this.staffService.delete(id).subscribe(() => {
      this.activeModal.close(ITEM_DELETED_EVENT);
    });
  }
}
