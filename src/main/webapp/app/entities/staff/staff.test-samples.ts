import dayjs from 'dayjs/esm';

import { IStaff, NewStaff } from './staff.model';

export const sampleWithRequiredData: IStaff = {
  id: 9933,
};

export const sampleWithPartialData: IStaff = {
  id: 3748,
  name: 'likely badly',
  phone: '+90-974-090-08-62',
  address: 'healthily temporariness shift',
  birthday: dayjs('2023-11-16T17:17'),
  salary: 21841,
};

export const sampleWithFullData: IStaff = {
  id: 3334,
  name: 'out excepting',
  email: 'Adalm8hs_Erbay6@gmail.com',
  phone: '+90-846-878-93-40',
  city: 'Osmaniye',
  address: 'ponce',
  birthday: dayjs('2023-11-16T02:54'),
  hireDate: dayjs('2023-11-16T04:18'),
  salary: 13209,
  role: dayjs('2023-11-16T19:22'),
  status: true,
};

export const sampleWithNewData: NewStaff = {
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
