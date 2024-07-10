import { Injectable } from '@angular/core';
import { FormGroup, FormControl, Validators } from '@angular/forms';

import { IQRCode, NewQRCode } from '../qr-code.model';

/**
 * A partial Type with required key is used as form input.
 */
type PartialWithRequiredKeyOf<T extends { id: unknown }> = Partial<Omit<T, 'id'>> & { id: T['id'] };

/**
 * Type for createFormGroup and resetForm argument.
 * It accepts IQRCode for edit and NewQRCodeFormGroupInput for create.
 */
type QRCodeFormGroupInput = IQRCode | PartialWithRequiredKeyOf<NewQRCode>;

type QRCodeFormDefaults = Pick<NewQRCode, 'id'>;

type QRCodeFormGroupContent = {
  id: FormControl<IQRCode['id'] | NewQRCode['id']>;
  code: FormControl<IQRCode['code']>;
  sessionId: FormControl<IQRCode['sessionId']>;
  athleteId: FormControl<IQRCode['athleteId']>;
  coachId: FormControl<IQRCode['coachId']>;
};

export type QRCodeFormGroup = FormGroup<QRCodeFormGroupContent>;

@Injectable({ providedIn: 'root' })
export class QRCodeFormService {
  createQRCodeFormGroup(qRCode: QRCodeFormGroupInput = { id: null }): QRCodeFormGroup {
    const qRCodeRawValue = {
      ...this.getFormDefaults(),
      ...qRCode,
    };
    return new FormGroup<QRCodeFormGroupContent>({
      id: new FormControl(
        { value: qRCodeRawValue.id, disabled: true },
        {
          nonNullable: true,
          validators: [Validators.required],
        },
      ),
      code: new FormControl(qRCodeRawValue.code, {
        validators: [Validators.required, Validators.minLength(5), Validators.maxLength(200)],
      }),
      sessionId: new FormControl(qRCodeRawValue.sessionId, {
        validators: [Validators.required],
      }),
      athleteId: new FormControl(qRCodeRawValue.athleteId, {
        validators: [Validators.required],
      }),
      coachId: new FormControl(qRCodeRawValue.coachId, {
        validators: [Validators.required],
      }),
    });
  }

  getQRCode(form: QRCodeFormGroup): IQRCode | NewQRCode {
    return form.getRawValue() as IQRCode | NewQRCode;
  }

  resetForm(form: QRCodeFormGroup, qRCode: QRCodeFormGroupInput): void {
    const qRCodeRawValue = { ...this.getFormDefaults(), ...qRCode };
    form.reset(
      {
        ...qRCodeRawValue,
        id: { value: qRCodeRawValue.id, disabled: true },
      } as any /* cast to workaround https://github.com/angular/angular/issues/46458 */,
    );
  }

  private getFormDefaults(): QRCodeFormDefaults {
    return {
      id: null,
    };
  }
}
