import { Injectable } from '@angular/core';
import { FormGroup, FormControl, Validators } from '@angular/forms';

import dayjs from 'dayjs/esm';
import { DATE_TIME_FORMAT } from 'app/config/input.constants';
import { ISessionPackage, NewSessionPackage } from '../session-package.model';

/**
 * A partial Type with required key is used as form input.
 */
type PartialWithRequiredKeyOf<T extends { id: unknown }> = Partial<Omit<T, 'id'>> & { id: T['id'] };

/**
 * Type for createFormGroup and resetForm argument.
 * It accepts ISessionPackage for edit and NewSessionPackageFormGroupInput for create.
 */
type SessionPackageFormGroupInput = ISessionPackage | PartialWithRequiredKeyOf<NewSessionPackage>;

/**
 * Type that converts some properties for forms.
 */
type FormValueOf<T extends ISessionPackage | NewSessionPackage> = Omit<T, 'startDate' | 'endDate'> & {
  startDate?: string | null;
  endDate?: string | null;
};

type SessionPackageFormRawValue = FormValueOf<ISessionPackage>;

type NewSessionPackageFormRawValue = FormValueOf<NewSessionPackage>;

type SessionPackageFormDefaults = Pick<NewSessionPackage, 'id' | 'startDate' | 'endDate'>;

type SessionPackageFormGroupContent = {
  id: FormControl<SessionPackageFormRawValue['id'] | NewSessionPackage['id']>;
  name: FormControl<SessionPackageFormRawValue['name']>;
  price: FormControl<SessionPackageFormRawValue['price']>;
  credits: FormControl<SessionPackageFormRawValue['credits']>;
  startDate: FormControl<SessionPackageFormRawValue['startDate']>;
  endDate: FormControl<SessionPackageFormRawValue['endDate']>;
  reviseCount: FormControl<SessionPackageFormRawValue['reviseCount']>;
  cancelCount: FormControl<SessionPackageFormRawValue['cancelCount']>;
};

export type SessionPackageFormGroup = FormGroup<SessionPackageFormGroupContent>;

@Injectable({ providedIn: 'root' })
export class SessionPackageFormService {
  createSessionPackageFormGroup(sessionPackage: SessionPackageFormGroupInput = { id: null }): SessionPackageFormGroup {
    const sessionPackageRawValue = this.convertSessionPackageToSessionPackageRawValue({
      ...this.getFormDefaults(),
      ...sessionPackage,
    });
    return new FormGroup<SessionPackageFormGroupContent>({
      id: new FormControl(
        { value: sessionPackageRawValue.id, disabled: true },
        {
          nonNullable: true,
          validators: [Validators.required],
        },
      ),
      name: new FormControl(sessionPackageRawValue.name),
      price: new FormControl(sessionPackageRawValue.price),
      credits: new FormControl(sessionPackageRawValue.credits),
      startDate: new FormControl(sessionPackageRawValue.startDate),
      endDate: new FormControl(sessionPackageRawValue.endDate),
      reviseCount: new FormControl(sessionPackageRawValue.reviseCount),
      cancelCount: new FormControl(sessionPackageRawValue.cancelCount),
    });
  }

  getSessionPackage(form: SessionPackageFormGroup): ISessionPackage | NewSessionPackage {
    return this.convertSessionPackageRawValueToSessionPackage(
      form.getRawValue() as SessionPackageFormRawValue | NewSessionPackageFormRawValue,
    );
  }

  resetForm(form: SessionPackageFormGroup, sessionPackage: SessionPackageFormGroupInput): void {
    const sessionPackageRawValue = this.convertSessionPackageToSessionPackageRawValue({ ...this.getFormDefaults(), ...sessionPackage });
    form.reset(
      {
        ...sessionPackageRawValue,
        id: { value: sessionPackageRawValue.id, disabled: true },
      } as any /* cast to workaround https://github.com/angular/angular/issues/46458 */,
    );
  }

  private getFormDefaults(): SessionPackageFormDefaults {
    const currentTime = dayjs();

    return {
      id: null,
      startDate: currentTime,
      endDate: currentTime,
    };
  }

  private convertSessionPackageRawValueToSessionPackage(
    rawSessionPackage: SessionPackageFormRawValue | NewSessionPackageFormRawValue,
  ): ISessionPackage | NewSessionPackage {
    return {
      ...rawSessionPackage,
      startDate: dayjs(rawSessionPackage.startDate, DATE_TIME_FORMAT),
      endDate: dayjs(rawSessionPackage.endDate, DATE_TIME_FORMAT),
    };
  }

  private convertSessionPackageToSessionPackageRawValue(
    sessionPackage: ISessionPackage | (Partial<NewSessionPackage> & SessionPackageFormDefaults),
  ): SessionPackageFormRawValue | PartialWithRequiredKeyOf<NewSessionPackageFormRawValue> {
    return {
      ...sessionPackage,
      startDate: sessionPackage.startDate ? sessionPackage.startDate.format(DATE_TIME_FORMAT) : undefined,
      endDate: sessionPackage.endDate ? sessionPackage.endDate.format(DATE_TIME_FORMAT) : undefined,
    };
  }
}
