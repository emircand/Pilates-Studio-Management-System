import dayjs from 'dayjs/esm';

export interface ISessionPackage {
  id: string;
  name?: string | null;
  price?: number | null;
  credits?: number | null;
  startDate?: dayjs.Dayjs | null;
  endDate?: dayjs.Dayjs | null;
  reviseCount?: number | null;
  cancelCount?: number | null;
}

export type NewSessionPackage = Omit<ISessionPackage, 'id'> & { id: null };
