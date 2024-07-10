import { Component, OnInit } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { finalize } from 'rxjs/operators';

import SharedModule from 'app/shared/shared.module';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { IStaff } from '../staff.model';
import { StaffService } from '../service/staff.service';
import { StaffFormService, StaffFormGroup } from './staff-form.service';

@Component({
  standalone: true,
  selector: 'jhi-staff-update',
  templateUrl: './staff-update.component.html',
  imports: [SharedModule, FormsModule, ReactiveFormsModule],
})
export class StaffUpdateComponent implements OnInit {
  isSaving = false;
  staff: IStaff | null = null;

  editForm: StaffFormGroup = this.staffFormService.createStaffFormGroup();

  constructor(
    protected staffService: StaffService,
    protected staffFormService: StaffFormService,
    protected activatedRoute: ActivatedRoute,
  ) {}

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ staff }) => {
      this.staff = staff;
      if (staff) {
        this.updateForm(staff);
      }
    });
  }

  previousState(): void {
    window.history.back();
  }

  save(): void {
    this.isSaving = true;
    const staff = this.staffFormService.getStaff(this.editForm);
    if (staff.id !== null) {
      this.subscribeToSaveResponse(this.staffService.update(staff));
    } else {
      this.subscribeToSaveResponse(this.staffService.create(staff));
    }
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<IStaff>>): void {
    result.pipe(finalize(() => this.onSaveFinalize())).subscribe({
      next: () => this.onSaveSuccess(),
      error: () => this.onSaveError(),
    });
  }

  protected onSaveSuccess(): void {
    this.previousState();
  }

  protected onSaveError(): void {
    // Api for inheritance.
  }

  protected onSaveFinalize(): void {
    this.isSaving = false;
  }

  protected updateForm(staff: IStaff): void {
    this.staff = staff;
    this.staffFormService.resetForm(this.editForm, staff);
  }
}
