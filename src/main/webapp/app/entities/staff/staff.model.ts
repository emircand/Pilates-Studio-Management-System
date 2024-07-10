import dayjs from 'dayjs/esm';

export interface IStaff {
  id: number;
  name?: string | null;
  email?: string | null;
  phone?: string | null;
  city?: string | null;
  address?: string | null;
  birthday?: dayjs.Dayjs | null;
  hireDate?: dayjs.Dayjs | null;
  salary?: number | null;
  role?: dayjs.Dayjs | null;
  status?: boolean | null;
}

export type NewStaff = Omit<IStaff, 'id'> & { id: null };
