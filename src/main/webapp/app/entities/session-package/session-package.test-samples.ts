import dayjs from 'dayjs/esm';

import { ISessionPackage, NewSessionPackage } from './session-package.model';

export const sampleWithRequiredData: ISessionPackage = {
  id: '08ff5a1e-35ad-4648-8bc4-105233b0653d',
};

export const sampleWithPartialData: ISessionPackage = {
  id: 'a985ab30-57a5-4e66-aa5a-7a1d347c8b42',
  name: 'after downforce exotic',
  startDate: dayjs('2023-11-16T03:10'),
  reviseCount: 13701,
};

export const sampleWithFullData: ISessionPackage = {
  id: 'aa40fe34-8063-4bf4-8ba2-9833bf15a7b2',
  name: 'pish',
  price: 1160,
  credits: 31112,
  startDate: dayjs('2023-11-16T01:04'),
  endDate: dayjs('2023-11-16T08:24'),
  reviseCount: 13261,
  cancelCount: 25936,
};

export const sampleWithNewData: NewSessionPackage = {
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
