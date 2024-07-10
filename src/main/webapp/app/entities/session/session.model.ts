import dayjs from 'dayjs/esm';
import { IStaff } from 'app/entities/staff/staff.model';
import { IAthlete } from 'app/entities/athlete/athlete.model';
import { SessionStatus } from 'app/entities/enumerations/session-status.model';

export interface ISession {
  id: number;
  startDate?: dayjs.Dayjs | null;
  endDate?: dayjs.Dayjs | null;
  qrCode?: string | null;
  sessionStatus?: keyof typeof SessionStatus | null;
  isNotified?: boolean | null;
  staff?: Pick<IStaff, 'id'> | null;
  athlete?: Pick<IAthlete, 'id'> | null;
}

export type NewSession = Omit<ISession, 'id'> & { id: null };
