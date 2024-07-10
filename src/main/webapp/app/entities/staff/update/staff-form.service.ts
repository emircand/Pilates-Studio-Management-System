import { Injectable } from '@angular/core';
import { FormGroup, FormControl, Validators } from '@angular/forms';

import dayjs from 'dayjs/esm';
import { DATE_TIME_FORMAT } from 'app/config/input.constants';
import { IStaff, NewStaff } from '../staff.model';

/**
 * A partial Type with required key is used as form input.
 */
type PartialWithRequiredKeyOf<T extends { id: unknown }> = Partial<Omit<T, 'id'>> & { id: T['id'] };

/**
 * Type for createFormGroup and resetForm argument.
 * It accepts IStaff for edit and NewStaffFormGroupInput for create.
 */
type StaffFormGroupInput = IStaff | PartialWithRequiredKeyOf<NewStaff>;

/**
 * Type that converts some properties for forms.
 */
type FormValueOf<T extends IStaff | NewStaff> = Omit<T, 'birthday' | 'hireDate' | 'role'> & {
  birthday?: string | null;
  hireDate?: string | null;
  role?: string | null;
};

type StaffFormRawValue = FormValueOf<IStaff>;

type NewStaffFormRawValue = FormValueOf<NewStaff>;

type StaffFormDefaults = Pick<NewStaff, 'id' | 'birthday' | 'hireDate' | 'role' | 'status'>;

type StaffFormGroupContent = {
  id: FormControl<StaffFormRawValue['id'] | NewStaff['id']>;
  name: FormControl<StaffFormRawValue['name']>;
  email: FormControl<StaffFormRawValue['email']>;
  phone: FormControl<StaffFormRawValue['phone']>;
  city: FormControl<StaffFormRawValue['city']>;
  address: FormControl<StaffFormRawValue['address']>;
  birthday: FormControl<StaffFormRawValue['birthday']>;
  hireDate: FormControl<StaffFormRawValue['hireDate']>;
  salary: FormControl<StaffFormRawValue['salary']>;
  role: FormControl<StaffFormRawValue['role']>;
  status: FormControl<StaffFormRawValue['status']>;
};

export type StaffFormGroup = FormGroup<StaffFormGroupContent>;

@Injectable({ providedIn: 'root' })
export class StaffFormService {
  createStaffFormGroup(staff: StaffFormGroupInput = { id: null }): StaffFormGroup {
    const staffRawValue = this.convertStaffToStaffRawValue({
      ...this.getFormDefaults(),
      ...staff,
    });
    return new FormGroup<StaffFormGroupContent>({
      id: new FormControl(
        { value: staffRawValue.id, disabled: true },
        {
          nonNullable: true,
          validators: [Validators.required],
        },
      ),
      name: new FormControl(staffRawValue.name),
      email: new FormControl(staffRawValue.email),
      phone: new FormControl(staffRawValue.phone),
      city: new FormControl(staffRawValue.city),
      address: new FormControl(staffRawValue.address),
      birthday: new FormControl(staffRawValue.birthday),
      hireDate: new FormControl(staffRawValue.hireDate),
      salary: new FormControl(staffRawValue.salary),
      role: new FormControl(staffRawValue.role),
      status: new FormControl(staffRawValue.status),
    });
  }

  getStaff(form: StaffFormGroup): IStaff | NewStaff {
    return this.convertStaffRawValueToStaff(form.getRawValue() as StaffFormRawValue | NewStaffFormRawValue);
  }

  resetForm(form: StaffFormGroup, staff: StaffFormGroupInput): void {
    const staffRawValue = this.convertStaffToStaffRawValue({ ...this.getFormDefaults(), ...staff });
    form.reset(
      {
        ...staffRawValue,
        id: { value: staffRawValue.id, disabled: true },
      } as any /* cast to workaround https://github.com/angular/angular/issues/46458 */,
    );
  }

  private getFormDefaults(): StaffFormDefaults {
    const currentTime = dayjs();

    return {
      id: null,
      birthday: currentTime,
      hireDate: currentTime,
      role: currentTime,
      status: false,
    };
  }

  private convertStaffRawValueToStaff(rawStaff: StaffFormRawValue | NewStaffFormRawValue): IStaff | NewStaff {
    return {
      ...rawStaff,
      birthday: dayjs(rawStaff.birthday, DATE_TIME_FORMAT),
      hireDate: dayjs(rawStaff.hireDate, DATE_TIME_FORMAT),
      role: dayjs(rawStaff.role, DATE_TIME_FORMAT),
    };
  }

  private convertStaffToStaffRawValue(
    staff: IStaff | (Partial<NewStaff> & StaffFormDefaults),
  ): StaffFormRawValue | PartialWithRequiredKeyOf<NewStaffFormRawValue> {
    return {
      ...staff,
      birthday: staff.birthday ? staff.birthday.format(DATE_TIME_FORMAT) : undefined,
      hireDate: staff.hireDate ? staff.hireDate.format(DATE_TIME_FORMAT) : undefined,
      role: staff.role ? staff.role.format(DATE_TIME_FORMAT) : undefined,
    };
  }
}
