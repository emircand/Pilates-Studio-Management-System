import { Injectable } from '@angular/core';
import { FormGroup, FormControl, Validators } from '@angular/forms';

import dayjs from 'dayjs/esm';
import { DATE_TIME_FORMAT } from 'app/config/input.constants';
import { ISession, NewSession } from '../session.model';

/**
 * A partial Type with required key is used as form input.
 */
type PartialWithRequiredKeyOf<T extends { id: unknown }> = Partial<Omit<T, 'id'>> & { id: T['id'] };

/**
 * Type for createFormGroup and resetForm argument.
 * It accepts ISession for edit and NewSessionFormGroupInput for create.
 */
type SessionFormGroupInput = ISession | PartialWithRequiredKeyOf<NewSession>;

/**
 * Type that converts some properties for forms.
 */
type FormValueOf<T extends ISession | NewSession> = Omit<T, 'startDate' | 'endDate'> & {
  startDate?: string | null;
  endDate?: string | null;
};

type SessionFormRawValue = FormValueOf<ISession>;

type NewSessionFormRawValue = FormValueOf<NewSession>;

type SessionFormDefaults = Pick<NewSession, 'id' | 'startDate' | 'endDate' | 'isNotified'>;

type SessionFormGroupContent = {
  id: FormControl<SessionFormRawValue['id'] | NewSession['id']>;
  startDate: FormControl<SessionFormRawValue['startDate']>;
  endDate: FormControl<SessionFormRawValue['endDate']>;
  qrCode: FormControl<SessionFormRawValue['qrCode']>;
  sessionStatus: FormControl<SessionFormRawValue['sessionStatus']>;
  isNotified: FormControl<SessionFormRawValue['isNotified']>;
  staff: FormControl<SessionFormRawValue['staff']>;
  athlete: FormControl<SessionFormRawValue['athlete']>;
};

export type SessionFormGroup = FormGroup<SessionFormGroupContent>;

@Injectable({ providedIn: 'root' })
export class SessionFormService {
  createSessionFormGroup(session: SessionFormGroupInput = { id: null }): SessionFormGroup {
    const sessionRawValue = this.convertSessionToSessionRawValue({
      ...this.getFormDefaults(),
      ...session,
    });
    return new FormGroup<SessionFormGroupContent>({
      id: new FormControl(
        { value: sessionRawValue.id, disabled: true },
        {
          nonNullable: true,
          validators: [Validators.required],
        },
      ),
      startDate: new FormControl(sessionRawValue.startDate),
      endDate: new FormControl(sessionRawValue.endDate),
      qrCode: new FormControl(sessionRawValue.qrCode),
      sessionStatus: new FormControl(sessionRawValue.sessionStatus),
      isNotified: new FormControl(sessionRawValue.isNotified),
      staff: new FormControl(sessionRawValue.staff),
      athlete: new FormControl(sessionRawValue.athlete),
    });
  }

  getSession(form: SessionFormGroup): ISession | NewSession {
    return this.convertSessionRawValueToSession(form.getRawValue() as SessionFormRawValue | NewSessionFormRawValue);
  }

  resetForm(form: SessionFormGroup, session: SessionFormGroupInput): void {
    const sessionRawValue = this.convertSessionToSessionRawValue({ ...this.getFormDefaults(), ...session });
    form.reset(
      {
        ...sessionRawValue,
        id: { value: sessionRawValue.id, disabled: true },
      } as any /* cast to workaround https://github.com/angular/angular/issues/46458 */,
    );
  }

  private getFormDefaults(): SessionFormDefaults {
    const currentTime = dayjs();

    return {
      id: null,
      startDate: currentTime,
      endDate: currentTime,
      isNotified: false,
    };
  }

  private convertSessionRawValueToSession(rawSession: SessionFormRawValue | NewSessionFormRawValue): ISession | NewSession {
    return {
      ...rawSession,
      startDate: dayjs(rawSession.startDate, DATE_TIME_FORMAT),
      endDate: dayjs(rawSession.endDate, DATE_TIME_FORMAT),
    };
  }

  private convertSessionToSessionRawValue(
    session: ISession | (Partial<NewSession> & SessionFormDefaults),
  ): SessionFormRawValue | PartialWithRequiredKeyOf<NewSessionFormRawValue> {
    return {
      ...session,
      startDate: session.startDate ? session.startDate.format(DATE_TIME_FORMAT) : undefined,
      endDate: session.endDate ? session.endDate.format(DATE_TIME_FORMAT) : undefined,
    };
  }
}
