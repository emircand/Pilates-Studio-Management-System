import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

import SharedModule from 'app/shared/shared.module';
import { ITEM_DELETED_EVENT } from 'app/config/navigation.constants';
import { IAthlete } from '../athlete.model';
import { AthleteService } from '../service/athlete.service';

@Component({
  standalone: true,
  templateUrl: './athlete-delete-dialog.component.html',
  imports: [SharedModule, FormsModule],
})
export class AthleteDeleteDialogComponent {
  athlete?: IAthlete;

  constructor(
    protected athleteService: AthleteService,
    protected activeModal: NgbActiveModal,
  ) {}

  cancel(): void {
    this.activeModal.dismiss();
  }

  confirmDelete(id: number): void {
    this.athleteService.delete(id).subscribe(() => {
      this.activeModal.close(ITEM_DELETED_EVENT);
    });
  }
}
