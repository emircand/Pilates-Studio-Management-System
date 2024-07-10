import dayjs from 'dayjs/esm';

import { ISession, NewSession } from './session.model';

export const sampleWithRequiredData: ISession = {
  id: 2747,
};

export const sampleWithPartialData: ISession = {
  id: 28912,
  startDate: dayjs('2023-11-16T00:51'),
  endDate: dayjs('2023-11-15T22:15'),
  qrCode: 'whispered even',
  sessionStatus: 'Canceled',
  isNotified: true,
};

export const sampleWithFullData: ISession = {
  id: 22634,
  startDate: dayjs('2023-11-16T04:57'),
  endDate: dayjs('2023-11-16T17:07'),
  qrCode: 'whereas standpoint',
  sessionStatus: 'Waiting',
  isNotified: true,
};

export const sampleWithNewData: NewSession = {
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
