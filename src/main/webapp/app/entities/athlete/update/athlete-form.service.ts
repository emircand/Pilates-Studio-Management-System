import { Injectable } from '@angular/core';
import { FormGroup, FormControl, Validators } from '@angular/forms';

import dayjs from 'dayjs/esm';
import { DATE_TIME_FORMAT } from 'app/config/input.constants';
import { IAthlete, NewAthlete } from '../athlete.model';

/**
 * A partial Type with required key is used as form input.
 */
type PartialWithRequiredKeyOf<T extends { id: unknown }> = Partial<Omit<T, 'id'>> & { id: T['id'] };

/**
 * Type for createFormGroup and resetForm argument.
 * It accepts IAthlete for edit and NewAthleteFormGroupInput for create.
 */
type AthleteFormGroupInput = IAthlete | PartialWithRequiredKeyOf<NewAthlete>;

/**
 * Type that converts some properties for forms.
 */
type FormValueOf<T extends IAthlete | NewAthlete> = Omit<T, 'birthday'> & {
  birthday?: string | null;
};

type AthleteFormRawValue = FormValueOf<IAthlete>;

type NewAthleteFormRawValue = FormValueOf<NewAthlete>;

type AthleteFormDefaults = Pick<NewAthlete, 'id' | 'birthday'>;

type AthleteFormGroupContent = {
  id: FormControl<AthleteFormRawValue['id'] | NewAthlete['id']>;
  name: FormControl<AthleteFormRawValue['name']>;
  email: FormControl<AthleteFormRawValue['email']>;
  phone: FormControl<AthleteFormRawValue['phone']>;
  city: FormControl<AthleteFormRawValue['city']>;
  address: FormControl<AthleteFormRawValue['address']>;
  birthday: FormControl<AthleteFormRawValue['birthday']>;
  sessionPackage: FormControl<AthleteFormRawValue['sessionPackage']>;
};

export type AthleteFormGroup = FormGroup<AthleteFormGroupContent>;

@Injectable({ providedIn: 'root' })
export class AthleteFormService {
  createAthleteFormGroup(athlete: AthleteFormGroupInput = { id: null }): AthleteFormGroup {
    const athleteRawValue = this.convertAthleteToAthleteRawValue({
      ...this.getFormDefaults(),
      ...athlete,
    });
    return new FormGroup<AthleteFormGroupContent>({
      id: new FormControl(
        { value: athleteRawValue.id, disabled: true },
        {
          nonNullable: true,
          validators: [Validators.required],
        },
      ),
      name: new FormControl(athleteRawValue.name),
      email: new FormControl(athleteRawValue.email),
      phone: new FormControl(athleteRawValue.phone),
      city: new FormControl(athleteRawValue.city),
      address: new FormControl(athleteRawValue.address),
      birthday: new FormControl(athleteRawValue.birthday),
      sessionPackage: new FormControl(athleteRawValue.sessionPackage),
    });
  }

  getAthlete(form: AthleteFormGroup): IAthlete | NewAthlete {
    return this.convertAthleteRawValueToAthlete(form.getRawValue() as AthleteFormRawValue | NewAthleteFormRawValue);
  }

  resetForm(form: AthleteFormGroup, athlete: AthleteFormGroupInput): void {
    const athleteRawValue = this.convertAthleteToAthleteRawValue({ ...this.getFormDefaults(), ...athlete });
    form.reset(
      {
        ...athleteRawValue,
        id: { value: athleteRawValue.id, disabled: true },
      } as any /* cast to workaround https://github.com/angular/angular/issues/46458 */,
    );
  }

  private getFormDefaults(): AthleteFormDefaults {
    const currentTime = dayjs();

    return {
      id: null,
      birthday: currentTime,
    };
  }

  private convertAthleteRawValueToAthlete(rawAthlete: AthleteFormRawValue | NewAthleteFormRawValue): IAthlete | NewAthlete {
    return {
      ...rawAthlete,
      birthday: dayjs(rawAthlete.birthday, DATE_TIME_FORMAT),
    };
  }

  private convertAthleteToAthleteRawValue(
    athlete: IAthlete | (Partial<NewAthlete> & AthleteFormDefaults),
  ): AthleteFormRawValue | PartialWithRequiredKeyOf<NewAthleteFormRawValue> {
    return {
      ...athlete,
      birthday: athlete.birthday ? athlete.birthday.format(DATE_TIME_FORMAT) : undefined,
    };
  }
}
