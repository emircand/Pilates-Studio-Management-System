import { Component, OnInit } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { finalize } from 'rxjs/operators';

import SharedModule from 'app/shared/shared.module';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { IQRCode } from '../qr-code.model';
import { QRCodeService } from '../service/qr-code.service';
import { QRCodeFormService, QRCodeFormGroup } from './qr-code-form.service';

@Component({
  standalone: true,
  selector: 'jhi-qr-code-update',
  templateUrl: './qr-code-update.component.html',
  imports: [SharedModule, FormsModule, ReactiveFormsModule],
})
export class QRCodeUpdateComponent implements OnInit {
  isSaving = false;
  qRCode: IQRCode | null = null;

  editForm: QRCodeFormGroup = this.qRCodeFormService.createQRCodeFormGroup();

  constructor(
    protected qRCodeService: QRCodeService,
    protected qRCodeFormService: QRCodeFormService,
    protected activatedRoute: ActivatedRoute,
  ) {}

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ qRCode }) => {
      this.qRCode = qRCode;
      if (qRCode) {
        this.updateForm(qRCode);
      }
    });
  }

  previousState(): void {
    window.history.back();
  }

  save(): void {
    this.isSaving = true;
    const qRCode = this.qRCodeFormService.getQRCode(this.editForm);
    if (qRCode.id !== null) {
      this.subscribeToSaveResponse(this.qRCodeService.update(qRCode));
    } else {
      this.subscribeToSaveResponse(this.qRCodeService.create(qRCode));
    }
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<IQRCode>>): void {
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

  protected updateForm(qRCode: IQRCode): void {
    this.qRCode = qRCode;
    this.qRCodeFormService.resetForm(this.editForm, qRCode);
  }
}
