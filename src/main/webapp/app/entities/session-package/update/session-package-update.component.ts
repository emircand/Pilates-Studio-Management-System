import { Component, OnInit } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { finalize } from 'rxjs/operators';

import SharedModule from 'app/shared/shared.module';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { ISessionPackage } from '../session-package.model';
import { SessionPackageService } from '../service/session-package.service';
import { SessionPackageFormService, SessionPackageFormGroup } from './session-package-form.service';

@Component({
  standalone: true,
  selector: 'jhi-session-package-update',
  templateUrl: './session-package-update.component.html',
  imports: [SharedModule, FormsModule, ReactiveFormsModule],
})
export class SessionPackageUpdateComponent implements OnInit {
  isSaving = false;
  sessionPackage: ISessionPackage | null = null;

  editForm: SessionPackageFormGroup = this.sessionPackageFormService.createSessionPackageFormGroup();

  constructor(
    protected sessionPackageService: SessionPackageService,
    protected sessionPackageFormService: SessionPackageFormService,
    protected activatedRoute: ActivatedRoute,
  ) {}

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ sessionPackage }) => {
      this.sessionPackage = sessionPackage;
      if (sessionPackage) {
        this.updateForm(sessionPackage);
      }
    });
  }

  previousState(): void {
    window.history.back();
  }

  save(): void {
    this.isSaving = true;
    const sessionPackage = this.sessionPackageFormService.getSessionPackage(this.editForm);
    if (sessionPackage.id !== null) {
      this.subscribeToSaveResponse(this.sessionPackageService.update(sessionPackage));
    } else {
      this.subscribeToSaveResponse(this.sessionPackageService.create(sessionPackage));
    }
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<ISessionPackage>>): void {
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

  protected updateForm(sessionPackage: ISessionPackage): void {
    this.sessionPackage = sessionPackage;
    this.sessionPackageFormService.resetForm(this.editForm, sessionPackage);
  }
}
