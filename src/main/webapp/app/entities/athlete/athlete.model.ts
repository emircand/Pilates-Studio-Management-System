import dayjs from 'dayjs/esm';
import { ISessionPackage } from 'app/entities/session-package/session-package.model';

export interface IAthlete {
  id: number;
  name?: string | null;
  email?: string | null;
  phone?: string | null;
  city?: string | null;
  address?: string | null;
  birthday?: dayjs.Dayjs | null;
  sessionPackage?: Pick<ISessionPackage, 'id'> | null;
}

export type NewAthlete = Omit<IAthlete, 'id'> & { id: null };
