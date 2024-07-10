import dayjs from 'dayjs/esm';

import { IAthlete, NewAthlete } from './athlete.model';

export const sampleWithRequiredData: IAthlete = {
  id: 29834,
};

export const sampleWithPartialData: IAthlete = {
  id: 24215,
  name: 'blah if',
  email: 'Bayancar66@yahoo.com',
  city: 'Gümüşhane',
  address: 'criticise psst',
  birthday: dayjs('2023-11-16T12:36'),
};

export const sampleWithFullData: IAthlete = {
  id: 5073,
  name: 'terribly',
  email: 'Alpkuluk28@gmail.com',
  phone: '+90-199-042-86-61',
  city: 'Denizli',
  address: 'thin',
  birthday: dayjs('2023-11-16T15:34'),
};

export const sampleWithNewData: NewAthlete = {
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
