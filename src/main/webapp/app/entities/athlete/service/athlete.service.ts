import { Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable, asapScheduler, scheduled } from 'rxjs';

import { catchError, map } from 'rxjs/operators';

import dayjs from 'dayjs/esm';

import { isPresent } from 'app/core/util/operators';
import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { createRequestOption } from 'app/core/request/request-util';
import { Search } from 'app/core/request/request.model';
import { IAthlete, NewAthlete } from '../athlete.model';

export type PartialUpdateAthlete = Partial<IAthlete> & Pick<IAthlete, 'id'>;

type RestOf<T extends IAthlete | NewAthlete> = Omit<T, 'birthday'> & {
  birthday?: string | null;
};

export type RestAthlete = RestOf<IAthlete>;

export type NewRestAthlete = RestOf<NewAthlete>;

export type PartialUpdateRestAthlete = RestOf<PartialUpdateAthlete>;

export type EntityResponseType = HttpResponse<IAthlete>;
export type EntityArrayResponseType = HttpResponse<IAthlete[]>;

@Injectable({ providedIn: 'root' })
export class AthleteService {
  protected resourceUrl = this.applicationConfigService.getEndpointFor('api/athletes');
  protected resourceSearchUrl = this.applicationConfigService.getEndpointFor('api/athletes/_search');

  constructor(
    protected http: HttpClient,
    protected applicationConfigService: ApplicationConfigService,
  ) {}

  create(athlete: NewAthlete): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(athlete);
    return this.http
      .post<RestAthlete>(this.resourceUrl, copy, { observe: 'response' })
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  update(athlete: IAthlete): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(athlete);
    return this.http
      .put<RestAthlete>(`${this.resourceUrl}/${this.getAthleteIdentifier(athlete)}`, copy, { observe: 'response' })
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  partialUpdate(athlete: PartialUpdateAthlete): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(athlete);
    return this.http
      .patch<RestAthlete>(`${this.resourceUrl}/${this.getAthleteIdentifier(athlete)}`, copy, { observe: 'response' })
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http
      .get<RestAthlete>(`${this.resourceUrl}/${id}`, { observe: 'response' })
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http
      .get<RestAthlete[]>(this.resourceUrl, { params: options, observe: 'response' })
      .pipe(map(res => this.convertResponseArrayFromServer(res)));
  }

  delete(id: number): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  search(req: Search): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http.get<RestAthlete[]>(this.resourceSearchUrl, { params: options, observe: 'response' }).pipe(
      map(res => this.convertResponseArrayFromServer(res)),
      catchError(() => scheduled([new HttpResponse<IAthlete[]>()], asapScheduler)),
    );
  }

  getAthleteIdentifier(athlete: Pick<IAthlete, 'id'>): number {
    return athlete.id;
  }

  compareAthlete(o1: Pick<IAthlete, 'id'> | null, o2: Pick<IAthlete, 'id'> | null): boolean {
    return o1 && o2 ? this.getAthleteIdentifier(o1) === this.getAthleteIdentifier(o2) : o1 === o2;
  }

  addAthleteToCollectionIfMissing<Type extends Pick<IAthlete, 'id'>>(
    athleteCollection: Type[],
    ...athletesToCheck: (Type | null | undefined)[]
  ): Type[] {
    const athletes: Type[] = athletesToCheck.filter(isPresent);
    if (athletes.length > 0) {
      const athleteCollectionIdentifiers = athleteCollection.map(athleteItem => this.getAthleteIdentifier(athleteItem)!);
      const athletesToAdd = athletes.filter(athleteItem => {
        const athleteIdentifier = this.getAthleteIdentifier(athleteItem);
        if (athleteCollectionIdentifiers.includes(athleteIdentifier)) {
          return false;
        }
        athleteCollectionIdentifiers.push(athleteIdentifier);
        return true;
      });
      return [...athletesToAdd, ...athleteCollection];
    }
    return athleteCollection;
  }

  protected convertDateFromClient<T extends IAthlete | NewAthlete | PartialUpdateAthlete>(athlete: T): RestOf<T> {
    return {
      ...athlete,
      birthday: athlete.birthday?.toJSON() ?? null,
    };
  }

  protected convertDateFromServer(restAthlete: RestAthlete): IAthlete {
    return {
      ...restAthlete,
      birthday: restAthlete.birthday ? dayjs(restAthlete.birthday) : undefined,
    };
  }

  protected convertResponseFromServer(res: HttpResponse<RestAthlete>): HttpResponse<IAthlete> {
    return res.clone({
      body: res.body ? this.convertDateFromServer(res.body) : null,
    });
  }

  protected convertResponseArrayFromServer(res: HttpResponse<RestAthlete[]>): HttpResponse<IAthlete[]> {
    return res.clone({
      body: res.body ? res.body.map(item => this.convertDateFromServer(item)) : null,
    });
  }
}
